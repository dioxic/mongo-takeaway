package uk.dioxic.mongotakeaway.generator;

import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import uk.dioxic.faker.Faker;
import uk.dioxic.mongotakeaway.domain.Customer;
import uk.dioxic.mongotakeaway.domain.AppSettings;
import uk.dioxic.mongotakeaway.domain.Event;
import uk.dioxic.mongotakeaway.event.CustomersReloadedEvent;
import uk.dioxic.mongotakeaway.event.PropertiesChangedEvent;
import uk.dioxic.mongotakeaway.service.EventService;
import uk.dioxic.mongotakeaway.service.GlobalLockService;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerGenerator {
    private final Random random = new Random();
    private final List<Customer> customers = new ArrayList<>();
    private final Faker faker= Faker.instance(Locale.UK);

    private final @NotNull ReactiveMongoTemplate mongoTemplate;
//    private final @NotNull CacheStateService cacheStateService;
    private final @NotNull GlobalLockService lockService;
    private final @NotNull EventService eventService;

    private AppSettings appSettings;

    private boolean reload() {
//        if (cacheStateService.isDirty("CACHE") && lockService.tryLock("CUSTOMER")) {
        if (lockService.tryLock("CUSTOMER")) {
            try {
                log.info("reloading");
                if (appSettings.dropCollection()) {
                    log.info("dropping customer collection");
                    mongoTemplate.dropCollection(Customer.class)
                            .doOnError(e -> log.error(e.getMessage(), e))
                            .block();
                }

                Flux<Customer> customerFlux = Flux.generate(
                        sink -> sink.next(generateCustomer(faker)));

                customers.clear();

                if (appSettings.customers() <= 0) {
                    mongoTemplate.remove(Customer.class).all()
                            .doOnSuccess(res -> log.info("purged all customers"))
                            .block();
                } else {
                    mongoTemplate.find(new Query().with(Sort.by("id")).skip(appSettings.customers()-1).limit(1), Customer.class)
                            .doOnError(e -> log.error(e.getMessage(), e))
                            .flatMap(customer -> mongoTemplate.remove(query(where("id").gt(customer.getId())), Customer.class))
                            .map(DeleteResult::getDeletedCount)
                            .filter(count -> count > 0)
                            .doOnNext(count -> log.info("purged {} superfluous customers", count))
                            .then(mongoTemplate.count(new Query(), Customer.class))
                            .map(count -> appSettings.customers() - count)
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
//                cacheStateService.markDirty("CACHE", false);
                eventService.publishEvent(CustomersReloadedEvent.class);
                return true;
            } finally {
                lockService.unlock("CUSTOMER");
            }
        }
        return false;
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

    private void refresh() {
        mongoTemplate.find(new Query().limit(appSettings.customers()), Customer.class)
                .doOnError(e -> log.error(e.getMessage(), e))
                .collectList()
                .doOnSuccess(customers::addAll)
                .doOnError(e -> log.error(e.getMessage(), e))
                .subscribe(customers -> log.info("loaded {} customers", customers.size()));
    }

    @Async
    @EventListener
    public void handlePropertiesChanged(PropertiesChangedEvent event) {
        appSettings = event.getSource();
        reload();
    }

    @Async
    @EventListener
    public void handleCustomersReloaded(CustomersReloadedEvent event) {
        refresh();
    }

}
