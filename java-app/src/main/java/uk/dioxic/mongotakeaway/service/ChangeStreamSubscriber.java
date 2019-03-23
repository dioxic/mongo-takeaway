package uk.dioxic.mongotakeaway.service;

import com.mongodb.annotations.ThreadSafe;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.text.CaseUtils;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.messaging.ChangeStreamRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import uk.dioxic.mongotakeaway.config.ChangeStreamProperties;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@ThreadSafe
public class ChangeStreamSubscriber<T, ID> {
    private static final String ALL = "ALL";
    private final DirectProcessor<ChangeStreamEvent<T>> processor;
    private final ReactiveMongoTemplate reactiveTemplate;
    private final ConcurrentHashMap<Object, AtomicInteger> externalSubscriptions = new ConcurrentHashMap<>();
    private InternalSubscriber csSubscriber;
    private ChangeStreamProperties properties;
    private volatile boolean resubscriptionScheduled = false;
    private Lock resubscriptionLock = new ReentrantLock();

    // config properties
    private Class<T> targetType;
    private List<String> operationTypes;
    private boolean returnFullDocumentOnUpdate;
    private Function<ChangeStreamEvent<T>, ID> extractKeyField;
    private Function<ID, Predicate<ChangeStreamEvent<T>>> postFilter;
    private BiFunction<Set<Object>,
            ChangeStreamOptions.ChangeStreamOptionsBuilder,
            ChangeStreamOptions.ChangeStreamOptionsBuilder> changeStreamOptions;

    @Builder
    ChangeStreamSubscriber(ReactiveMongoTemplate reactiveTemplate,
                           ChangeStreamProperties properties,
                           Class<T> targetType,
                           BiFunction<Set<Object>, ChangeStreamOptions.ChangeStreamOptionsBuilder,
                                   ChangeStreamOptions.ChangeStreamOptionsBuilder> changeStreamOptions,
                           List<String> operationTypes,
                           String keyField,
                           boolean returnFullDocumentOnUpdate,
                           Function<BsonValue, ID> documentIdConverter,
                           Function<ChangeStreamEvent<T>, ID> extractKeyField,
                           Function<ID, Predicate<ChangeStreamEvent<T>>> postFilter) {
        Objects.requireNonNull(reactiveTemplate, "reactiveTemplate");
        Objects.requireNonNull(properties, "properties");
        Objects.requireNonNull(targetType, "targetType");

        this.reactiveTemplate = reactiveTemplate;
        this.properties = properties;
        this.targetType = targetType;
        this.postFilter = postFilter;
        this.returnFullDocumentOnUpdate = returnFullDocumentOnUpdate;
        this.operationTypes = Optional.ofNullable(operationTypes).orElse(List.of("insert", "update"));
        this.changeStreamOptions = Optional.ofNullable(changeStreamOptions).orElse(this::defaultChangeStreamOptions);

        if (postFilter == null) {
            if (extractKeyField != null)
                this.postFilter = id -> (cse -> id.equals(extractKeyField.apply(cse)));
            else if (documentIdConverter != null)
                this.postFilter = id -> (cse -> id.equals(documentIdConverter.apply(getDocumentKey(cse))));
            else
                this.postFilter = id -> (cse -> true);
        }

        processor = DirectProcessor.create();
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(this::resubscribe,
                        1,
                        properties.getResubscriptionInterval(),
                        TimeUnit.SECONDS);
    }

    private HashSet<Object> getSubscriptionKeySet() {
        return new HashSet<>(externalSubscriptions.keySet());
    }

    private ChangeStreamOptions.ChangeStreamOptionsBuilder defaultChangeStreamOptions(Set<Object> subs, ChangeStreamOptions.ChangeStreamOptionsBuilder builder) {
        Criteria criteria = where("operationType").in(operationTypes);
        if (subs != null && !subs.isEmpty() && !subs.contains(ALL)) {
            criteria = criteria.and("documentKey._id").in(subs);
        }

        builder = builder.filter(newAggregation(match(criteria)));
        if (returnFullDocumentOnUpdate) {
            builder = builder.returnFullDocumentOnUpdate();
        }
        return builder;
    }

    private void resubscribe() {
        if (resubscriptionScheduled && resubscriptionLock.tryLock()) {
            resubscriptionScheduled = false;

            try {
                ChangeStreamOptions.ChangeStreamOptionsBuilder builder = ChangeStreamOptions.builder();
                Set<Object> subs = new HashSet<>(externalSubscriptions.keySet());

                if (csSubscriber != null) {
                    csSubscriber.cancel();
                    csSubscriber.dispose();
                    csSubscriber.applyResumeToken(builder);
                    // ensure the resume token can only be used for this resubscription iteration
                    csSubscriber = null;
                }

                if (!subs.isEmpty()) {
                    // deferred subscribe
                    log.info("pausing {} sec before {} changestream creation", properties.getSubscriptionPause(), targetType.getSimpleName());
                    Thread.sleep(properties.getSubscriptionPause() * 1000);

                    ChangeStreamOptions options = changeStreamOptions.apply(subs, builder).build();

                    reactiveTemplate
                            .changeStream(getCollectionName(targetType), options, targetType)
                            .doOnNext(o -> log.trace("receiving {} notification for {}", Objects.requireNonNull(o.getOperationType()).getValue(), Objects.requireNonNull(o.getBody())))
                            .doOnSubscribe(x -> log.info("{} changestream created with token={}", targetType.getSimpleName(), options.getResumeToken()
                                    .filter(BsonValue::isDocument)
                                    .map(BsonValue::asDocument)
                                    .map(doc -> doc.get("_data"))
                                    .filter(BsonValue::isString)
                                    .map(BsonValue::asString)
                                    .map(BsonString::getValue)
                                    .orElse("null")))
                            .doOnComplete(() -> log.info("{} changestream  complete", targetType.getSimpleName()))
                            .doOnCancel(() -> log.info("{} changestream cancelled", targetType.getSimpleName()))
                            .doOnError(e -> log.error(e.getMessage()))
                            .subscribe(csSubscriber = new InternalSubscriber(processor, csSubscriber));
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            } finally {
                resubscriptionLock.unlock();
            }
        }
    }

    private String getCollectionName(Class<T> targetType) {
        return Optional.ofNullable(targetType.getAnnotation(Document.class))
                .map(Document::collection)
                .filter(coll -> !coll.isEmpty())
                .orElseGet(() -> transformClassNameToCollection(targetType));
    }

    private String transformClassNameToCollection(Class<T> targetType) {
        return Character.toLowerCase(targetType.getSimpleName().charAt(0)) + targetType.getSimpleName().substring(1);
    }

    public Flux<ChangeStreamEvent<T>> subscribe() {
        externalSubscriptions.computeIfAbsent(ALL, c -> {
            log.info("subscribing to ALL {}", targetType.getSimpleName());
            resubscriptionScheduled = true;
            return new AtomicInteger(0);
        }).incrementAndGet();

        return processor;
    }

    public void unsubscribe() {
        externalSubscriptions.computeIfPresent(ALL, (c, atomic) -> {
            if (atomic.decrementAndGet() <= 0) {
                log.info("unsubscribing to ALL {}", targetType.getSimpleName());
                resubscriptionScheduled = true;
                return null;
            } else
                return atomic;
        });
    }

    public Flux<ChangeStreamEvent<T>> subscribe(ID id) {
        externalSubscriptions.computeIfAbsent(id, c -> {
            log.info("subscribing to {} [id={}]", targetType.getSimpleName(), id);
            resubscriptionScheduled = true;
            return new AtomicInteger(0);
        }).incrementAndGet();

        return processor
                .filter(postFilter.apply(id));
    }

    public void unsubscribe(ID id) {
        externalSubscriptions.computeIfPresent(id, (c, atomic) -> {
            if (atomic.decrementAndGet() <= 0) {
                log.info("unsubscribing to {} [id={}]", targetType.getSimpleName(), id);
                resubscriptionScheduled = true;
                return null;
            } else
                return atomic;
        });
    }

    public static BsonValue getDocumentKey(ChangeStreamEvent<?> cse) {
        return Optional.ofNullable(cse)
                .map(ChangeStreamEvent::getRaw)
                .map(ChangeStreamDocument::getDocumentKey)
                .map(docKey -> docKey.get("_id"))
        .get();
    }

    public static ObjectId getDocumentKeyAsObjectId(ChangeStreamEvent<?> cse) {
        return getDocumentKey(cse).asObjectId().getValue();
    }

    private class InternalSubscriber extends BaseSubscriber<ChangeStreamEvent<T>> {
        private DirectProcessor<ChangeStreamEvent<T>> processor;
        private BsonValue resumeToken;

        InternalSubscriber(DirectProcessor<ChangeStreamEvent<T>> processor, InternalSubscriber previous) {
            this(processor);
            if (previous != null && previous.getResumeToken() != null)
                this.resumeToken = previous.getResumeToken();
        }

        InternalSubscriber(DirectProcessor<ChangeStreamEvent<T>> processor) {
            Objects.requireNonNull(processor, "processor cannot be null");
            this.processor = processor;
        }

        @Override
        protected void hookOnNext(ChangeStreamEvent<T> value) {
            resumeToken = value.getResumeToken();
            processor.sink().next(value);
        }

        void applyResumeToken(ChangeStreamOptions.ChangeStreamOptionsBuilder builder) {
            if (resumeToken != null) {
                builder.resumeToken(resumeToken);
            }
        }

        BsonValue getResumeToken() {
            return resumeToken;
        }
    }

}
