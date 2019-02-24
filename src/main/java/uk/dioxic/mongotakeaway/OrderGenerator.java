package uk.dioxic.mongotakeaway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Indexes;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.config.GeneratorProperties;

import java.time.temporal.ChronoUnit;
import java.util.Random;

import static java.time.LocalDateTime.now;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

@Slf4j
@Component
@EnableScheduling
public class OrderGenerator implements CommandLineRunner {

    private static final ObjectMapper json = new ObjectMapper();

    private MongoClient client;
    private MongoTemplate mongoTemplate;
    private GeneratorProperties properties;

    public OrderGenerator(MongoClient client, MongoTemplate mongoTemplate, GeneratorProperties properties) {
        this.client = client;
        this.mongoTemplate = mongoTemplate;
        this.properties = properties;
    }

    private Flux<Order> orderFlux = Flux.generate(
            () -> new Order(0,0, Order.State.PENDING, now(), now()),
            (state, sink) -> {
                Order order = new Order(state.getId()+1,
                        (state.getCustomerId()+1) % properties.getCustomers(),
                        Order.State.PENDING,
                        now(),
                        now());
                sink.next(order);
                return order;
            });

    @Scheduled(initialDelay = 2000, fixedDelay = 5000)
    public void updateJob() {
        log.debug("performing scheduled update tasks");

        long modified = mongoTemplate.updateMulti(query(
                where("state").is(Order.State.PENDING)
                        .and("created").lt(now().minus(properties.getPendingTime(), ChronoUnit.SECONDS))),
                update("state", Order.State.ONROUTE).currentDate("modified"),
                Order.class)
        .getModifiedCount();

        log.debug("transitioned {} from {} to {}", modified, Order.State.PENDING, Order.State.ONROUTE);

        modified = mongoTemplate.updateMulti(query(
                where("state").is(Order.State.ONROUTE)
                        .and("modified").lt(now().minus(properties.getOnrouteTime(), ChronoUnit.SECONDS))),
                update("state", Order.State.DELIVERED).currentDate("modified"),
                Order.class)
        .getModifiedCount();

        log.debug("transitioned {} from {} to {}", modified, Order.State.ONROUTE, Order.State.DELIVERED);

        modified = mongoTemplate.remove(query(where("modified").lt(now().minus(properties.getTtl(), ChronoUnit.SECONDS))),
                Order.class)
        .getDeletedCount();

        log.debug("deleted {} old orders", modified);
    }

    @Override
    public void run(String... args) {
        MongoCollection<Document> collection = client.getDatabase("test").getCollection("order");

        log.info("dropping existing orders");
        Mono.from(collection.drop())
            .block();

        Mono.from(collection.createIndex(Indexes.ascending("created")))
            .block();

        Mono.from(collection.createIndex(Indexes.ascending("modified")))
            .block();

        Random random = new Random();

        log.info("generating new orders (rate={}/s)", properties.getRate());
        orderFlux
//                .delayElements(Duration.ofMillis(100))
                .doOnNext(doc -> {
                    if (properties.getRate() > 0) {
                        try {
                            Thread.sleep(random.nextInt(2000 / properties.getRate() + 1));
                        } catch (InterruptedException e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                })
                .doOnNext(order -> log.trace("taking {}", order))
                .map(mongoTemplate::insert)
                .onErrorContinue((e, o) -> {
                        e.printStackTrace();
                        log.warn("error [{}] writing order", e.getMessage());
                })
//                .buffer(1)
//                .flatMap(collection::bulkWrite)
//                .map(BulkWriteResult::getInsertedCount)
                .blockLast();

        log.info("done");

    }

}
