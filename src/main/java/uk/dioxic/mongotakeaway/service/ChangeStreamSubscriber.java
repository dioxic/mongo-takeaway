package uk.dioxic.mongotakeaway.service;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
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
public class ChangeStreamSubscriber<T, ID> {

    private DirectProcessor<ChangeStreamEvent<T>> processor;
    private ReactiveMongoTemplate reactiveTemplate;
    private ConcurrentHashMap<ID, AtomicInteger> externalSubscriptions = new ConcurrentHashMap<>();
    private InternalSubscriber csSubscriber;
    private ChangeStreamProperties properties;
    private volatile boolean resubscriptionScheduled = false;
    private Lock resubscriptionLock = new ReentrantLock();

    // config properties
    private Class<T> targetType;
    private List<String> operationTypes;
    private Function<ChangeStreamEvent<T>, ID> extractKeyField;
    private Function<ID, Predicate<ChangeStreamEvent<T>>> postFilter;
    private BiFunction<Set<ID>,
            ChangeStreamOptions.ChangeStreamOptionsBuilder,
            ChangeStreamOptions.ChangeStreamOptionsBuilder> changeStreamOptions;

    @Builder
    ChangeStreamSubscriber(ReactiveMongoTemplate reactiveTemplate,
                           ChangeStreamProperties properties,
                           Class<T> targetType,
                           BiFunction<Set<ID>, ChangeStreamOptions.ChangeStreamOptionsBuilder, ChangeStreamOptions.ChangeStreamOptionsBuilder> changeStreamOptions,
                           List<String> operationTypes,
                           String keyField,
                           Function<ChangeStreamEvent<T>, ID> extractKeyField,
                           Function<ID, Predicate<ChangeStreamEvent<T>>> postFilter) {
        Objects.requireNonNull(reactiveTemplate, "reactiveTemplate");
        Objects.requireNonNull(properties, "properties");
        Objects.requireNonNull(targetType, "targetType");

        this.reactiveTemplate = reactiveTemplate;
        this.properties = properties;
        this.targetType = targetType;
        this.postFilter = postFilter;
        this.operationTypes = Optional.ofNullable(operationTypes).orElse(List.of("insert", "update"));
        this.changeStreamOptions = Optional.ofNullable(changeStreamOptions).orElse(defaultChangeStreamOptions);

        if (postFilter == null) {
            if (extractKeyField != null)
                this.postFilter = id -> (cse -> id.equals(extractKeyField.apply(cse)));
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

    private HashSet<ID> getSubscriptionKeySet() {
        return new HashSet<>(externalSubscriptions.keySet());
    }

    private BiFunction<Set<ID>, ChangeStreamOptions.ChangeStreamOptionsBuilder, ChangeStreamOptions.ChangeStreamOptionsBuilder> defaultChangeStreamOptions =
            (subs, builder) -> builder.filter(newAggregation(match(
                    where("documentKey._id").in(subs)
                    .and("operationType").in(operationTypes)
            )));

    private void resubscribe() {
        if (resubscriptionScheduled && resubscriptionLock.tryLock()) {
            resubscriptionScheduled = false;

            try {
                ChangeStreamOptions.ChangeStreamOptionsBuilder builder = ChangeStreamOptions.builder();
                Set<ID> subs = new HashSet<>(externalSubscriptions.keySet());

                if (csSubscriber != null) {
                    csSubscriber.cancel();
                    csSubscriber.dispose();
                    csSubscriber.applyResumeToken(builder);
                    // ensure the resume token can only be used for this resubscription iteration
                    csSubscriber = null;
                }

                if (!subs.isEmpty()) {
                    // deferred subscribe
                    log.info("pausing {} sec before changestream creation", properties.getSubscriptionPause());
                    Thread.sleep(properties.getSubscriptionPause() * 1000);

                    ChangeStreamOptions options = changeStreamOptions.apply(subs, builder).build();

                    reactiveTemplate
                            .changeStream(options, targetType)
                            .doOnNext(o -> log.trace("receiving {} notification for {}", Objects.requireNonNull(o.getOperationType()).getValue(), Objects.requireNonNull(o.getBody())))
                            .doOnSubscribe(x -> log.info("changestream created for subscriptions {} with token={}", subs, options.getResumeToken()
                                    .filter(BsonValue::isDocument)
                                    .map(BsonValue::asDocument)
                                    .map(doc -> doc.get("_data"))
                                    .filter(BsonValue::isString)
                                    .map(BsonValue::asString)
                                    .map(BsonString::getValue)
                                    .orElse("null")))
                            .doOnComplete(() -> log.info("changestream complete"))
                            .doOnCancel(() -> log.info("changestream cancelled"))
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

    public Flux<ChangeStreamEvent<T>> subscribe(ID id) {
        externalSubscriptions.computeIfAbsent(id, c -> {
            log.info("subscribing id {}", id);
            resubscriptionScheduled = true;
            return new AtomicInteger(0);
        }).incrementAndGet();

        return processor
                .filter(postFilter.apply(id));
    }

    public void unsubscribe(ID id) {
        externalSubscriptions.computeIfPresent(id, (c, atomic) -> {
            if (atomic.decrementAndGet() <= 0) {
                log.info("unsubscribing id {}", id);
                resubscriptionScheduled = true;
                return null;
            } else
                return atomic;
        });
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
