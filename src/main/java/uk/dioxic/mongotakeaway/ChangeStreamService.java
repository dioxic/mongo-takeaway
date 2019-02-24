package uk.dioxic.mongotakeaway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.changestream.FullDocument;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.BsonNull;
import org.bson.BsonString;
import org.bson.BsonValue;
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
import java.util.stream.Collectors;

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
    private ConcurrentHashMap<Integer, AtomicInteger> subscriptionTokens = new ConcurrentHashMap<>();
    private ChangeStreamSubscriber<Order> csSubscriber;
    private GeneratorProperties properties;
    private volatile boolean resubscriptionScheduled = false;

    public ChangeStreamService(ReactiveMongoTemplate reactiveTemplate, GeneratorProperties properties) {
        this.reactiveTemplate = reactiveTemplate;
        this.properties = properties;
        processor = DirectProcessor.create();
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(this::resubscribe,
                        1,
                        Math.max(properties.getResubscriptionInterval(), properties.getSubscriptionPause()+1),
                        TimeUnit.SECONDS);
    }

    private void resubscribe() {
        if (!subscriptionTokens.isEmpty() && resubscriptionScheduled) {
            resubscriptionScheduled = false;
            Set<Integer> tokens = new HashSet<>(subscriptionTokens.keySet());

            ChangeStreamOptions.ChangeStreamOptionsBuilder builder = ChangeStreamOptions.builder()
                    .filter(newAggregation(match(
                            where("operationType").in("insert", "update")
                                    .and("ns.coll").is("order")
                                    .and("fullDocument._id").exists(true)
                                    .and("fullDocument.customerId").in(tokens)))).fullDocumentLookup(FullDocument.UPDATE_LOOKUP);

            if (csSubscriber != null) {
                csSubscriber.cancel();
                csSubscriber.dispose();
                csSubscriber.applyResumeToken(builder);
            }

            // deferred subscribe
            log.info("pausing {} sec before changestream creation", properties.getSubscriptionPause());
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                ChangeStreamOptions options = builder.build();

                reactiveTemplate
                    .changeStream("test", "order", options, Order.class)
                    .doOnNext(o -> log.trace("receiving {} notification for order={}", Objects.requireNonNull(o.getOperationType()).getValue(), Objects.requireNonNull(o.getBody())))
                    .doOnSubscribe(x -> log.info("changestream created for customers {} with token={}", tokens, options.getResumeToken()
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
                    .subscribe(csSubscriber = new ChangeStreamSubscriber<>(processor));

            }, properties.getSubscriptionPause(), TimeUnit.SECONDS);
        }
    }

    public Flux<ChangeStreamEvent<Order>> subscribe(Integer customer) {
        log.info("subscribing customer {}", customer);
        subscriptionTokens.computeIfAbsent(customer, c -> new AtomicInteger(0));
        subscriptionTokens.computeIfPresent(customer, (c, atomic) -> {
            atomic.incrementAndGet();
            return atomic;
        });

        resubscriptionScheduled = true;

        return processor;
    }

    public void unsubscribe(Integer customer) {
        log.info("unsubscribing customer {}", customer);

        subscriptionTokens.computeIfPresent(customer, (c, atomic) -> (atomic.decrementAndGet() <= 0) ? null : atomic);
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

        void applyResumeToken(ChangeStreamOptions.ChangeStreamOptionsBuilder builder) {
            if (resumeToken != null) {
                builder.resumeToken(resumeToken);
            }
        }

    }

}
