package uk.dioxic.mongotakeaway.service;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import uk.dioxic.mongotakeaway.config.ChangeStreamProperties;
import uk.dioxic.mongotakeaway.domain.Customer;
import uk.dioxic.mongotakeaway.domain.Order;

@Slf4j
@Service
public class OrderService extends AbstractChangeStreamService<Order, ObjectId> {

    public OrderService(ReactiveMongoTemplate reactiveTemplate, ChangeStreamProperties properties) {
        super(reactiveTemplate, properties);
    }

    public Flux<ChangeStreamEvent<Order>> subscribe(Customer customer) {
        return subscribe(new ObjectId(customer.getId()));
    }

    public void unsubscribe(Customer customer) {
        unsubscribe(new ObjectId(customer.getId()));
    }

    @Override
    Class<Order> getTargetType() {
        return Order.class;
    }

    @Override
    String getKeyField() {
        return "customerId";
    }

    @Override
    ObjectId resolveFilterValue(Order document) {
        return document.getCustomerId();
    }


}
