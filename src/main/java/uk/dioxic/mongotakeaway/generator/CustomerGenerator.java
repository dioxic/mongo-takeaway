package uk.dioxic.mongotakeaway.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import uk.dioxic.faker.Faker;
import uk.dioxic.mongotakeaway.config.GeneratorProperties;
import uk.dioxic.mongotakeaway.domain.Customer;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class CustomerGenerator {
    private final Random random = new Random();
    private final ReactiveMongoTemplate mongoTemplate;
    private final GeneratorProperties properties;
    private List<Customer> generatedItems;
    private volatile boolean dbInitialised;

    public CustomerGenerator(ReactiveMongoTemplate mongoTemplate, GeneratorProperties properties) {
        this.mongoTemplate = mongoTemplate;
        this.properties = properties;
        if (properties.getRate() != 0) {
            Executors.newSingleThreadScheduledExecutor()
                    .schedule(this::runGenerateJob,
                            0,
                            TimeUnit.SECONDS);
        }
    }

    public synchronized void initialiseDatabase() {
        if (!dbInitialised && properties.isDropCollection()) {
            log.info("dropping existing customers");
            mongoTemplate.dropCollection(Customer.class)
                    .doOnError(e -> log.error(e.getMessage(), e))
                    .block();

            dbInitialised = true;
        }
    }

    private static Customer generateCustomer(Faker faker) {
        return new Customer(faker.get("name.first_name"), faker.get("name.last_name"));
    }

    public void runGenerateJob() {
        initialiseDatabase();

        log.info("generating new customers");

        Faker faker = Faker.instance(Locale.UK);

        Flux<Customer> customerFlux = Flux.generate(
                sink -> sink.next(generateCustomer(faker)));

        generatedItems = customerFlux
                .take(properties.getCustomers())
                .buffer(1000)
                .flatMap(mongoTemplate::insertAll)
                .onErrorContinue((e, o) -> {
                    e.printStackTrace();
                    log.warn("error [{}] writing customer", e.getMessage());
                })
                .collectList()
                .doOnNext(list -> log.info("created {} customers", list.size()))
                .block();
    }

    public Customer getRandomGeneratedCustomer() {
        return generatedItems.get(random.nextInt(generatedItems.size()));
    }

    public Customer getItem(int index) {
        return generatedItems.get(index);
    }

    private int currentIdx = 0;
    private Lock nextLock = new ReentrantLock();

    public Customer getNext() {
        nextLock.lock();

        try {
            if (currentIdx == generatedItems.size()) {
                currentIdx = 0;
            }
            return generatedItems.get(currentIdx++);
        } finally {
            nextLock.unlock();
        }
    }

}
