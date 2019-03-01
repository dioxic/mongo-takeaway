package uk.dioxic.mongotakeaway;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import uk.dioxic.mongotakeaway.config.GeneratorProperties;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.time.LocalDateTime.now;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

@Slf4j
@Component
public class OrderGenerator {

    private final ReactiveMongoTemplate mongoTemplate;
    private final GeneratorProperties properties;
    private volatile boolean dbInitialised;

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

    public synchronized void initialiseDatabase() {
        if (!dbInitialised && properties.isDropCollection()) {
            log.info("dropping existing orders");
            mongoTemplate.dropCollection(Order.class)
                    .doOnError(e -> log.error(e.getMessage(), e))
                    .block();

            Index modifyIdx= new Index("modified", ASC);
            if (properties.getTtl() > 0) {
                // technically redundant due to batch job (but why not)
                modifyIdx = modifyIdx.expire(properties.getTtl()+1, TimeUnit.SECONDS);
            }

            log.info("create indexes on orders collection");
            mongoTemplate.indexOps(Order.class).ensureIndex(new Index("created", ASC))
                    .and(mongoTemplate.indexOps(Order.class).ensureIndex(modifyIdx))
                    .doOnError(e -> log.error(e.getMessage(), e))
                    .block();

            dbInitialised = true;
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

    public <T> T delay(T t) {
        if (properties.getRate() > 0) {
            Random random = new Random();
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
        return t;
    }

    public void runGenerateJob() {
        initialiseDatabase();

        log.info("generating new orders (rate={}/s, batchSize={})", properties.getRate(), properties.adjustedBatchSize());

        try {
            Flux<Order> orderFlux = Flux.generate(
                () -> new Order(1, 0L),
                (state, sink) -> {
                    Order order = new Order((int)(state.getThreadId()+1) % properties.getCustomers(), state.getThreadId()+1);
                    sink.next(order);
                    return order;
            });

            AtomicLong totalCount = new AtomicLong();
            AtomicLong lastCount = new AtomicLong();

            orderFlux
                .doOnNext(this::delay)
                .buffer(properties.adjustedBatchSize())
                .publishOn(Schedulers.elastic())
//                .doOnNext(order -> log.info("accepted {}", order))
                .doOnNext(orders -> {
                    if (orders.size() == 1)
                        log.trace("accepted {}", orders.get(0));
                    else
                        log.trace("accepted {} orders", orders.size());
                })
                .flatMap(orders -> mongoTemplate.insert(orders, "order"), properties.getConcurrency())
                .onErrorContinue((e, o) -> {
                    e.printStackTrace();
                    log.warn("error [{}] writing order", e.getMessage());
                })
                .doOnNext(o -> totalCount.incrementAndGet())
                .sample(Duration.ofSeconds(5))
                .doOnNext(order -> log.info("accepted {} orders in total ({}/s)", totalCount.get(), (totalCount.get() - lastCount.get()) / 5))
                .doOnNext(order -> lastCount.set(totalCount.get()))
                .sample(Duration.ofSeconds(20))
                .doOnNext(order -> log.info("sample {}", order))
                .blockLast();

        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("done");

    }

}
