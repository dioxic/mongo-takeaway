package uk.dioxic.mongotakeaway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import uk.dioxic.mongotakeaway.config.GeneratorProperties;

import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.time.LocalDateTime.now;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

@Slf4j
@Component
public class OrderGenerator {

    private ReactiveMongoTemplate mongoTemplate;
    private GeneratorProperties properties;

    public OrderGenerator(ReactiveMongoTemplate mongoTemplate, GeneratorProperties properties) {
        this.mongoTemplate = mongoTemplate;
        this.properties = properties;
        if (properties.getJobInterval() > 0) {
            Executors.newSingleThreadScheduledExecutor()
                    .scheduleAtFixedRate(this::runScheduledJob,
                            1,
                            properties.getJobInterval(),
                            TimeUnit.SECONDS);
        }
        if (properties.getRate() != 0) {
            Executors.newSingleThreadScheduledExecutor()
                    .schedule(this::runGenerateJob,
                            1,
                            TimeUnit.SECONDS);
        }
    }

    public void runScheduledJob() {
        log.debug("performing scheduled tasks");

        if (properties.getPendingTime() > 0) {
            mongoTemplate.updateMulti(query(
                    where("state").is(Order.State.PENDING)
                            .and("created").lt(now().minus(properties.getPendingTime(), ChronoUnit.SECONDS))),
                    update("state", Order.State.ONROUTE).currentDate("modified"),
                    Order.class)
                    .map(UpdateResult::getModifiedCount)
                    .subscribe(modified -> log.debug("transitioned {} from {} to {}", modified, Order.State.PENDING, Order.State.ONROUTE));
        }

        if (properties.getOnrouteTime() > 0) {
            mongoTemplate.updateMulti(query(
                    where("state").is(Order.State.ONROUTE)
                            .and("modified").lt(now().minus(properties.getOnrouteTime(), ChronoUnit.SECONDS))),
                    update("state", Order.State.DELIVERED).currentDate("modified"),
                    Order.class)
                    .map(UpdateResult::getModifiedCount)
                    .subscribe(modified -> log.debug("transitioned {} from {} to {}", modified, Order.State.ONROUTE, Order.State.DELIVERED));
        }

        if (properties.getTtl() > 0) {
            mongoTemplate.remove(query(where("modified").lt(now().minus(properties.getTtl(), ChronoUnit.SECONDS))),
                    Order.class)
                    .map(DeleteResult::getDeletedCount)
                    .subscribe(deleted -> log.debug("deleted {} old orders", deleted));
        }
    }

    public void runGenerateJob() {
        log.info("dropping existing orders");
        mongoTemplate.dropCollection(Order.class)
                .block();

        Index modifyIdx= new Index("modified", ASC);
        if (properties.getTtl() > 0) {
            // technically redundant due to batch job (but why not)
            modifyIdx = modifyIdx.expire(properties.getTtl()+1, TimeUnit.SECONDS);
        }

        log.info("create indexes on orders collection");
        mongoTemplate.indexOps(Order.class).ensureIndex(new Index("created", ASC))
                .and(mongoTemplate.indexOps(Order.class).ensureIndex(modifyIdx))
                .block();

        Random random = new Random();

        Consumer<Object> delay = order -> {
            if (properties.getRate() > 0) {
                try {
                    long interval = properties.isRandomise() ?
                            random.nextInt(2000 / properties.getRate()) :
                            1000 / properties.getRate();

                    Thread.sleep(interval);
                }
                catch (InterruptedException e) {
                    throw Exceptions.propagate(e);
                }
            }
        };

        log.info("generating new orders (rate={}/s)", properties.getRate());
        Flux.generate(
            () -> new Order(0L,0),
            (state, sink) -> {
                Order order = new Order(state.getId()+1,
                        (state.getCustomerId()+1) % properties.getCustomers());
                sink.next(order);
                return order;
        })
        .doOnNext(delay)
        .doOnNext(order -> log.trace("taking {}", order))
        .flatMap(mongoTemplate::insert)
        .onErrorContinue((e, o) -> {
                e.printStackTrace();
                log.warn("error [{}] writing order", e.getMessage());
        })
        .blockLast();

        log.info("done");

    }

}
