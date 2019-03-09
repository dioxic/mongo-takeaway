package uk.dioxic.mongotakeaway.config;

import com.mongodb.client.model.changestream.FullDocument;
import org.bson.BsonValue;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import uk.dioxic.mongotakeaway.domain.Customer;
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
                                .fullDocumentLookup(FullDocument.UPDATE_LOOKUP)
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
                .documentIdCoverter(bson -> bson.asObjectId().getValue())
                .build();
    }

    @Bean("customerByIdSubscriber")
    public ChangeStreamSubscriber<Customer, ObjectId> customerByIdSubscriber(ReactiveMongoTemplate reactiveTemplate, ChangeStreamProperties properties) {
        return ChangeStreamSubscriber.<Customer, ObjectId>builder()
                .reactiveTemplate(reactiveTemplate)
                .properties(properties)
                .targetType(Customer.class)
                .operationTypes(List.of("insert", "update", "delete"))
                .documentIdCoverter(bson -> bson.asObjectId().getValue())
                .build();
    }
}
