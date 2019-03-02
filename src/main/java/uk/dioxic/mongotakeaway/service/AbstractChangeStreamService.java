package uk.dioxic.mongotakeaway.service;

import com.mongodb.client.model.changestream.FullDocument;
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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Deprecated
public abstract class AbstractChangeStreamService<T, ID> {

    private DirectProcessor<ChangeStreamEvent<T>> processor;
    private ReactiveMongoTemplate reactiveTemplate;
    private ConcurrentHashMap<ID, AtomicInteger> externalSubscriptions = new ConcurrentHashMap<>();
    private ChangeStreamSubscriber csSubscriber;
    private ChangeStreamProperties properties;
    private volatile boolean resubscriptionScheduled = false;
    private Lock resubscriptionLock = new ReentrantLock();

    public AbstractChangeStreamService(ReactiveMongoTemplate reactiveTemplate, ChangeStreamProperties properties) {
        this.reactiveTemplate = reactiveTemplate;
        this.properties = properties;
        processor = DirectProcessor.create();
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(this::resubscribe,
                        1,
                        properties.getResubscriptionInterval(),
                        TimeUnit.SECONDS);
    }

    abstract Class<T> getTargetType();

    abstract String getKeyField();

    abstract ID resolveFilterValue(T document);

    final HashSet<ID> getSubscriptionKeySet() {
        return new HashSet<>(externalSubscriptions.keySet());
    }

    List<String> getOperationTypes() {
        return List.of("insert", "update");
    }

    Predicate<ChangeStreamEvent<T>> postFilter(ID id) {
        return cse -> id.equals(resolveFilterValue(cse.getBody()));
    }

    void initialiseBuilder(ChangeStreamOptions.ChangeStreamOptionsBuilder builder, Set<ID> subs) {
        builder.filter(newAggregation(match(
                where("operationType").in(getOperationTypes())
                .and("fullDocument." + getKeyField()).in(subs)))
        )
        .fullDocumentLookup(FullDocument.UPDATE_LOOKUP);
    }

    private void resubscribe() {
        if (resubscriptionScheduled && resubscriptionLock.tryLock()) {
            resubscriptionScheduled = false;

            try {
                ChangeStreamOptions.ChangeStreamOptionsBuilder builder = ChangeStreamOptions.builder();
                Set<ID> subs = new HashSet<>(externalSubscriptions.keySet());
                initialiseBuilder(builder, subs);

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
                    ChangeStreamOptions options = builder.build();

                    csSubscriber = new ChangeStreamSubscriber(processor, csSubscriber);

                    reactiveTemplate
                            .changeStream(options, getTargetType())
                            .doOnNext(o -> log.trace("receiving {} notification for order={}", Objects.requireNonNull(o.getOperationType()).getValue(), Objects.requireNonNull(o.getBody())))
                            .doOnSubscribe(x -> log.info("changestream created for customers {} with token={}", subs, options.getResumeToken()
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
                            .subscribe(csSubscriber = new ChangeStreamSubscriber(processor, csSubscriber));
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
                .filter(postFilter(id));
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

    private class ChangeStreamSubscriber extends BaseSubscriber<ChangeStreamEvent<T>> {
        private DirectProcessor<ChangeStreamEvent<T>> processor;
        private BsonValue resumeToken;

        ChangeStreamSubscriber(DirectProcessor<ChangeStreamEvent<T>> processor, ChangeStreamSubscriber previous) {
            this(processor);
            if (previous != null && previous.getResumeToken() != null)
                this.resumeToken = previous.getResumeToken();
        }

        ChangeStreamSubscriber(DirectProcessor<ChangeStreamEvent<T>> processor) {
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
