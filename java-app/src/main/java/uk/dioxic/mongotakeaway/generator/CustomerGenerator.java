package uk.dioxic.mongotakeaway.generator;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.dioxic.faker.Faker;
import uk.dioxic.mongotakeaway.config.GeneratorProperties;
import uk.dioxic.mongotakeaway.domain.Customer;
import uk.dioxic.mongotakeaway.domain.GlobalProperties;
import uk.dioxic.mongotakeaway.service.ChangeStreamSubscriber;
import uk.dioxic.mongotakeaway.service.GlobalPropertyService;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class CustomerGenerator implements Runnable {
    private final static CountDownLatch dbInitialised = new CountDownLatch(1);
    private final static Lock initialiseLock = new ReentrantLock();
    private final Random random = new Random();
    private final List<Customer> customers = new ArrayList<>();
    private final ReactiveMongoTemplate mongoTemplate;
    private final GlobalPropertyService propertyService;

    public CustomerGenerator(ReactiveMongoTemplate mongoTemplate,
                             GlobalPropertyService propertyService) {
        this.mongoTemplate = mongoTemplate;
        this.propertyService = propertyService;
        propertyService.addListener(this);

//        if (customers.size() < propertyService.getProperties().generator().getCustomers()) {
//            Executors.newSingleThreadScheduledExecutor()
//                    .schedule(this::runGenerateJob,
//                            0,
//                            TimeUnit.SECONDS);
//        }
    }

    @Override
    public void run() {
        if (customers.size() < propertyService.getProperties().generator().getCustomers()) {
            runGenerateJob();
        }
    }

    /**
     * Blocks until the DB is initialised.
     * <p/>
     * Returns instantly if the DB has already been initialised.
     */
    private void initialiseDatabaseIfNeccessary() {
        if (dbInitialised.getCount() > 0) {
            if (initialiseLock.tryLock()) {
                try {
                    if (propertyService.getProperties().generator().isDropCollection()) {
                        log.info("dropping existing customers");
                        mongoTemplate.dropCollection(Customer.class)
                                .doOnError(e -> log.error(e.getMessage(), e))
                                .block();
                    } else {
                        log.info("loading existing customers");
                        List<Customer> existingCustomers = mongoTemplate.findAll(Customer.class)
                                .doOnError(e -> log.error(e.getMessage(), e))
                                .collectList()
                                .block();

                        if (existingCustomers != null) {
                            customers.addAll(existingCustomers);
                        }
                    }
                } finally {
                    dbInitialised.countDown();
                    initialiseLock.unlock();
                }
            }
            try {
                dbInitialised.await();
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    private static Customer generateCustomer(Faker faker) {
        return new Customer(faker.get("name.first_name"), faker.get("name.last_name"));
    }

    public void runGenerateJob() {
        initialiseDatabaseIfNeccessary();

        log.info("generating new customers");

        Faker faker = Faker.instance(Locale.UK);

        Flux<Customer> customerFlux = Flux.generate(
                sink -> sink.next(generateCustomer(faker)));

        List<Customer> generatedItems = customerFlux
                .take(propertyService.getProperties().generator().getCustomers() - customers.size())
                .buffer(1000)
                .flatMap(mongoTemplate::insertAll)
                .onErrorContinue((e, o) -> {
                    e.printStackTrace();
                    log.warn("error [{}] writing customer", e.getMessage());
                })
                .collectList()
                .doOnNext(list -> log.info("created {} customers", list.size()))
                .block();

        if (generatedItems != null)
            customers.addAll(generatedItems);
    }

    public Customer getRandomCustomer() {
        return customers.get(random.nextInt(customers.size()));
    }

    public Customer getItem(int index) {
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

}
