package uk.dioxic.mongotakeaway.generator;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.dioxic.faker.Faker;
import uk.dioxic.mongotakeaway.config.GeneratorProperties;
import uk.dioxic.mongotakeaway.domain.Customer;
import uk.dioxic.mongotakeaway.domain.GlobalProperties;
import uk.dioxic.mongotakeaway.event.CustomersReloadedEvent;
import uk.dioxic.mongotakeaway.event.PropertiesChangedEvent;
import uk.dioxic.mongotakeaway.service.ChangeStreamSubscriber;
import uk.dioxic.mongotakeaway.service.EventService;
import uk.dioxic.mongotakeaway.service.GlobalLockService;
import uk.dioxic.mongotakeaway.service.GlobalPropertyService;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.data.mongodb.core.query.Criteria.*;
import static org.springframework.data.mongodb.core.query.Query.*;

@Slf4j
@Component
public class CustomerGenerator {
    //    private final static CountDownLatch dbInitialised = new CountDownLatch(1);
    private final static Lock initialiseLock = new ReentrantLock();
    private final Random random = new Random();
    private final List<Customer> customers = new ArrayList<>();
    private final ReactiveMongoTemplate mongoTemplate;
    private final Faker faker;
    private GlobalProperties globalProperties;
    private GlobalLockService lockService;
    private EventService eventService;

    public CustomerGenerator(ReactiveMongoTemplate mongoTemplate, GlobalLockService lockService, EventService eventService) {
        this.mongoTemplate = mongoTemplate;
        this.lockService = lockService;
        this.eventService = eventService;
        this.faker = Faker.instance(Locale.UK);
    }

    /**
     * Blocks until the DB is initialised.
     * <p/>
     * Returns instantly if the DB has already been initialised.
     */
    private void initialiseDatabaseIfNeccessary() {
        if (lockService.tryLock("CUSTOMER")) {
            try {
                if (globalProperties.generator().isDropCollection()) {
                    log.info("dropping customer collection");
                    mongoTemplate.dropCollection(Customer.class)
                            .doOnError(e -> log.error(e.getMessage(), e))
                            .block();
                }

                Flux<Customer> customerFlux = Flux.generate(
                        sink -> sink.next(generateCustomer(faker)));

                customers.clear();

                if (globalProperties.generator().getCustomers() <= 0) {
                    mongoTemplate.remove(Customer.class).all()
                            .doOnSuccess(res -> log.info("purged all customers"))
                            .block();
                } else {
                    mongoTemplate.find(new Query().with(Sort.by("_id")).skip(globalProperties.generator().getCustomers()-1).limit(1), Customer.class)
                            .doOnError(e -> log.error(e.getMessage(), e))
                            .flatMap(customer -> mongoTemplate.remove(query(where("id").gt(customer.getId())), Customer.class))
                            .doOnNext(res -> log.info("purged {} superfluous customers", res.getDeletedCount()))
                            .then(mongoTemplate.count(new Query(), Customer.class))
                            .map(count -> globalProperties.generator().getCustomers() - count)
                            .filter(count -> count > 0)
                            .flatMap(count -> customerFlux.take(count)
                                    .buffer(1000)
                                    .flatMap(mongoTemplate::insertAll)
                                    .onErrorContinue((e, o) -> {
                                        e.printStackTrace();
                                        log.warn("error [{}] writing customer", e.getMessage());
                                    })
                                    .reduce(0, (size, customer) -> size + 1)
                                    .doOnNext(list -> log.info("created {} customers", list))
                            )
                            .block();
                }
                eventService.publishEvent(new CustomersReloadedEvent("reload"));
            } finally {
                lockService.unlock("CUSTOMER");
            }
        }
    }

    private static Customer generateCustomer(Faker faker) {
        return new Customer(faker.get("name.first_name"), faker.get("name.last_name"));
    }

    private Function<List<Customer>, List<ObjectId>> extractIds =
            customers -> customers.stream().map(Customer::getId).collect(Collectors.toList());

    public Customer getRandomCustomer() {
        return customers.get(random.nextInt(customers.size()));
    }

    public Customer get(int index) {
        return customers.get(index);
    }

    private volatile int currentIdx = 0;
    private Lock nextLock = new ReentrantLock();

    public Customer getNext() {
        nextLock.lock();

        try {
            if (currentIdx == customers.size()) {
                currentIdx = 0;
            }
            return customers.get(currentIdx++);
        } finally {
            nextLock.unlock();
        }
    }

    @Async
    @EventListener
    public void handlePropertiesChanged(PropertiesChangedEvent event) {
        globalProperties = event.getSource();
        initialiseDatabaseIfNeccessary();
    }

    @Async
    @EventListener
    public void handleCustomersReloaded(CustomersReloadedEvent event) {
        mongoTemplate.find(new Query().limit(globalProperties.generator().getCustomers()), Customer.class)
                .doOnError(e -> log.error(e.getMessage(), e))
                .collectList()
                .doOnSuccess(customers::addAll)
                .doOnError(e -> log.error(e.getMessage(), e))
                .subscribe(customers -> log.info("loaded {} customers", customers.size()));
    }


}
