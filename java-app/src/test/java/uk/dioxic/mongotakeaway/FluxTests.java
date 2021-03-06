package uk.dioxic.mongotakeaway;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.ConnectableFlux;
import uk.dioxic.mongotakeaway.domain.Order;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Disabled
@DataMongoTest
@ExtendWith(SpringExtension.class)
public class FluxTests {

    @Autowired
    private ReactiveMongoTemplate template;

    @Test
    public void connectableFluxTest() {

        ChangeStreamOptions.ChangeStreamOptionsBuilder builder = ChangeStreamOptions.builder()
                .filter(newAggregation(match(where("operationType").is("insert")
                        .and("ns.coll").is("order")
                        .and("fullDocument._id").exists(true))));

        log.info("subscribing to changestream");
        ConnectableFlux<ChangeStreamEvent<Order>> flux  = template
                .changeStream("test", "order", builder.build(), Order.class)
                .doOnSubscribe(x -> log.info("subscribed to order feed")).publish();

        flux.connect();

        flux.subscribe(System.out::println);

        flux.blockLast();

    }

}
