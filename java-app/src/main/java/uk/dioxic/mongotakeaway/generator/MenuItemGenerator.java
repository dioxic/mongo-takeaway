package uk.dioxic.mongotakeaway.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import uk.dioxic.faker.Faker;
import uk.dioxic.mongotakeaway.config.GeneratorProperties;
import uk.dioxic.mongotakeaway.domain.MenuItem;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MenuItemGenerator {
    private final Random random = new Random();
    private final ReactiveMongoTemplate mongoTemplate;
    private final GeneratorProperties properties;
    private List<MenuItem> generatedItems;
    private volatile boolean dbInitialised;

    public MenuItemGenerator(ReactiveMongoTemplate mongoTemplate, GeneratorProperties properties) {
        this.mongoTemplate = mongoTemplate;
        this.properties = properties;
        if (properties.getRate() != 0) {
            Executors.newSingleThreadScheduledExecutor()
                    .schedule(this::runGenerateJob,
                            0,
                            TimeUnit.SECONDS);
        }
    }

    private synchronized void initialiseDatabase() {
        if (!dbInitialised && properties.isDropCollection()) {
            log.info("dropping existing menu items");
            mongoTemplate.dropCollection(MenuItem.class)
                    .doOnError(e -> log.error(e.getMessage(), e))
                    .block();

            dbInitialised = true;
        }
    }

    private MenuItem generateMenuItem(Faker faker) {
        return new MenuItem(faker.get("commerce.product_name.adjective") + " " + faker.get("food.ingredients") +" with " + faker.get("food.spices"),
                faker.get("hipster.words"),
                generatePrice()
        );
    }

    private BigDecimal generatePrice() {
        double price = 2 + random.nextDouble()*15;
        return new BigDecimal(price, new MathContext(10000, RoundingMode.CEILING))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void runGenerateJob() {
        initialiseDatabase();

        log.info("generating new menu items");

        Faker faker = Faker.instance(Locale.UK);

        try {
            Flux<MenuItem> customerFlux = Flux.generate(
                    sink -> sink.next(generateMenuItem(faker)));

            generatedItems = customerFlux
                .take(properties.getMenuItems())
                .buffer(1000)
                .flatMap(mongoTemplate::insertAll)
                .onErrorContinue((e, o) -> {
                    e.printStackTrace();
                    log.warn("error [{}] writing menu item", e.getMessage());
                })
                .collectList()
                .doOnNext(list -> log.info("created {} menu items", list.size()))
                .block();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MenuItem getRandomGeneratedItem() {
        return generatedItems.get(random.nextInt(generatedItems.size()));
    }

}
