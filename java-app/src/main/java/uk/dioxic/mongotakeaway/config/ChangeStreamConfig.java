package uk.dioxic.mongotakeaway.config;

import com.mongodb.client.model.changestream.FullDocument;
import org.bson.BsonValue;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import uk.dioxic.mongotakeaway.domain.Customer;
import uk.dioxic.mongotakeaway.domain.Event;
import uk.dioxic.mongotakeaway.domain.GlobalProperties;
import uk.dioxic.mongotakeaway.domain.Order;
import uk.dioxic.mongotakeaway.service.ChangeStreamSubscriber;

import java.util.List;
import java.util.Objects;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Configuration
public class ChangeStreamConfig {

    @Bean("orderByCustomerIdSubscriber")
    public ChangeStreamSubscriber<Order, ObjectId> orderByCustomerIdSubscriber(ReactiveMongoTemplate reactiveTemplate, ChangeStreamProperties properties) {
        return ChangeStreamSubscriber.<Order, ObjectId>builder()
                .reactiveTemplate(reactiveTemplate)
                .properties(properties)
                .targetType(Order.class)
                .extractKeyField(cse -> Objects.requireNonNull(cse.getBody()).getCustomerId())
                .changeStreamOptions(
                        (subs, builder) -> builder.filter(newAggregation(match(
                                where("fullDocument.customerId").in(subs)
                                        .and("operationType").in("insert", "update"))))
                                .returnFullDocumentOnUpdate()
                )
                .build();
    }

    @Bean("orderByIdSubscriber")
    public ChangeStreamSubscriber<Order, ObjectId> orderByIdSubscriber(ReactiveMongoTemplate reactiveTemplate, ChangeStreamProperties properties) {
        return ChangeStreamSubscriber.<Order, ObjectId>builder()
                .reactiveTemplate(reactiveTemplate)
                .properties(properties)
                .targetType(Order.class)
                .operationTypes(List.of("insert"))
                .documentIdConverter(bson -> bson.asObjectId().getValue())
                .build();
    }

    @Bean("customerByIdSubscriber")
    public ChangeStreamSubscriber<Customer, ObjectId> customerByIdSubscriber(ReactiveMongoTemplate reactiveTemplate, ChangeStreamProperties properties) {
        return ChangeStreamSubscriber.<Customer, ObjectId>builder()
                .reactiveTemplate(reactiveTemplate)
                .properties(properties)
                .targetType(Customer.class)
                .operationTypes(List.of("insert", "update", "delete"))
                .postFilter((id -> cse -> id.equals(ChangeStreamSubscriber.getDocumentKeyAsObjectId(cse))))
                .build();
    }

    @Bean("globalPropertiesFlux")
    public Flux<ChangeStreamEvent<GlobalProperties>> orderFlux(ReactiveMongoTemplate reactiveTemplate) {
        return reactiveTemplate.changeStream(ChangeStreamSubscriber.getCollectionName(GlobalProperties.class), ChangeStreamOptions.builder().build(), GlobalProperties.class);
    }

    @Bean("eventFlux")
    public Flux<ChangeStreamEvent<Event>> eventFlux(ReactiveMongoTemplate reactiveTemplate) {
        return reactiveTemplate.changeStream(ChangeStreamSubscriber.getCollectionName(Event.class),
                ChangeStreamOptions.builder()
                        .filter(newAggregation(match(where("operationType").in("insert"))))
                        .build(),
                Event.class);
    }

    @Bean("globalProperties")
    public ChangeStreamSubscriber<GlobalProperties, String> globalPropertiesSubscriber(ReactiveMongoTemplate reactiveTemplate, ChangeStreamProperties properties) {
        return ChangeStreamSubscriber.<GlobalProperties, String>builder()
                .reactiveTemplate(reactiveTemplate)
                .properties(properties)
                .targetType(GlobalProperties.class)
                .operationTypes(List.of("insert", "update", "delete", "replace"))
                .returnFullDocumentOnUpdate(true)
                .documentIdConverter(bson -> bson.asString().getValue())
                .build();
    }
}
