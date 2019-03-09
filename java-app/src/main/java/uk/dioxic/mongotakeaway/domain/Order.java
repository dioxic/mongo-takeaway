package uk.dioxic.mongotakeaway.domain;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;

@Data
@Document
public class Order {
    @Id
    private String id;
    private Long threadId;
    private ObjectId customerId;
    private State state = State.CREATED;
    private LocalDateTime created = now();
    private LocalDateTime modified = now();
    private List<OrderLine> items;

    public Order() {
    }

    public Order(Order order) {
        this.threadId = order.threadId;
        this.customerId = order.customerId;
        this.state = order.state;
        this.created = order.created;
        this.modified = order.modified;
    }

    public Order(ObjectId customerId, Long threadId) {
        this.customerId = customerId;
        this.threadId = threadId;
    }

    public enum State {
        CREATED, ACCEPTED, PENDING, COOKING, ONROUTE, DELIVERED
    }

    public Order onroute() {
        state = State.ONROUTE;
        return this;
    }

    public Order pending() {
        state = State.PENDING;
        return this;
    }

    public Order delivered() {
        state = State.DELIVERED;
        return this;
    }

    public Order addItem(OrderLine item) {
        if (item == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        return this;
    }

    @Data
    public static class OrderLine {
        private ObjectId itemId;
        private String name;
        private int quantity;
        private BigDecimal price;
    }

}