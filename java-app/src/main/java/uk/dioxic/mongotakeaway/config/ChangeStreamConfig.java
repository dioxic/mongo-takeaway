package uk.dioxic.mongotakeaway.config;

import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;
import uk.dioxic.mongotakeaway.domain.Customer;
import uk.dioxic.mongotakeaway.domain.Event;
import uk.dioxic.mongotakeaway.domain.AppSettings;
import uk.dioxic.mongotakeaway.domain.Order;
import uk.dioxic.mongotakeaway.service.ChangeStreamSubscriber;

import java.util.List;
import java.util.Objects;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static uk.dioxic.mongotakeaway.service.ChangeStreamSubscriber.getDocumentKeyAsObjectId;

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
                .postFilter((id -> cse -> id.equals(getDocumentKeyAsObjectId(cse))))
                .build();
    }

}
