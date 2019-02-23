package uk.dioxic.mongotakeaway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.changestream.FullDocument;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import uk.dioxic.mongotakeaway.config.GeneratorProperties;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Service
public class ChangeStreamService {

    private static final ObjectMapper json = new ObjectMapper();
    private static final Criteria filterCriteria = where("operationType").is("insert")
            .and("ns.coll").is("order")
            .and("fullDocument._id").exists(true);
    private static final Aggregation csPipeline = newAggregation(match(filterCriteria));

    private DirectProcessor<ChangeStreamEvent<Order>> processor;
    private ReactiveMongoTemplate reactiveTemplate;
    private Map<Integer, AtomicInteger> subscriptionTokens = new ConcurrentHashMap<>();
    private ChangeStreamSubscriber<Order> csSubscriber;
    private GeneratorProperties properties;
    private volatile boolean resubscriptionScheduled = false;

    public ChangeStreamService(ReactiveMongoTemplate reactiveTemplate, GeneratorProperties properties) {
        this.reactiveTemplate = reactiveTemplate;
        this.properties = properties;
        processor = DirectProcessor.create();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::resubscribe, 1, properties.getResubscriptionInterval(), TimeUnit.SECONDS);
    }

    public void unsubscribe(Integer customer) {
        log.info("unsubscribing customer {}", customer);

        if (subscriptionTokens.containsKey(customer)) {
            AtomicInteger subscriptions = subscriptionTokens.get(customer);
            int count = subscriptions.decrementAndGet();
            if (count <= 0)
                subscriptionTokens.remove(customer);
        }
    }

    private void resubscribe() {
        if (!subscriptionTokens.isEmpty() && resubscriptionScheduled) {

            ChangeStreamOptions.ChangeStreamOptionsBuilder builder = ChangeStreamOptions.builder()
                    .filter(newAggregation(match(
                            where("operationType").in("insert", "update")
                                    .and("ns.coll").is("order")
                                    .and("fullDocument._id").exists(true)
                                    .and("fullDocument.customerId").in(subscriptionTokens.keySet())))).fullDocumentLookup(FullDocument.UPDATE_LOOKUP);

            if (csSubscriber != null) {
                csSubscriber.cancel();
                csSubscriber.dispose();
                builder.resumeToken(csSubscriber.getResumeToken());
            }

            // deferred subscribe
            log.info("pausing {} sec before changestream creation", properties.getSubscriptionPause());
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                log.info("subscribing to changestream for customers {} with token={}", subscriptionTokens.keySet(), csSubscriber == null ? "null" : csSubscriber.getResumeToken());

                Flux<ChangeStreamEvent<Order>> cs = reactiveTemplate
                        .changeStream("test", "order", builder.build(), Order.class)
//                    .doOnNext(o -> log.info("receiving notification for customer={}", Objects.requireNonNull(o.getBody()).getCustomerId()))
                        .doOnSubscribe(x -> log.info("subscription to changestream"))
                        .doOnComplete(() -> log.info("changestream complete"))
                        .doOnCancel(() -> log.info("changestream cancelled"))
                        .doOnError(e -> log.error(e.getMessage()));

                cs.subscribe(csSubscriber = new ChangeStreamSubscriber<>(processor));
                resubscriptionScheduled = false;
            }, properties.getSubscriptionPause(), TimeUnit.SECONDS);
        }
    }

    public Flux<ChangeStreamEvent<Order>> subscribe(Integer customer) {
        log.info("subscribing customer {}", customer);
        AtomicInteger count = subscriptionTokens.get(customer);
        if (count == null)
            subscriptionTokens.put(customer, new AtomicInteger(1));
        else
            count.incrementAndGet();

        resubscriptionScheduled = true;

        return processor;
    }

    class ChangeStreamSubscriber<T> extends BaseSubscriber<ChangeStreamEvent<T>> {
        private DirectProcessor<ChangeStreamEvent<T>> processor;
        private BsonDocument resumeToken;

        ChangeStreamSubscriber(DirectProcessor<ChangeStreamEvent<T>> processor) {
            this.processor = processor;
        }

        @Override
        protected void hookOnNext(ChangeStreamEvent<T> value) {
            resumeToken = Objects.requireNonNull(value.getRaw()).getResumeToken();
            processor.sink().next(value);
        }

        BsonDocument getResumeToken() {
            return resumeToken;
        }
    }

}
