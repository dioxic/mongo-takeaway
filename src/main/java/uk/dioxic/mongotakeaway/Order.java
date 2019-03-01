package uk.dioxic.mongotakeaway;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Data
@Document
public class Order {
    @Id
    private String id;
    private Long threadId;
    private Integer customerId;
    private State state = State.PENDING;
    private LocalDateTime created = now();
    private LocalDateTime modified = now();

    public Order() {
    }

    public Order(Order order) {
        this.threadId = order.threadId;
        this.customerId = order.customerId;
        this.state = order.state;
        this.created = order.created;
        this.modified = order.modified;
    }

    public Order(Integer customerId, Long threadId) {
        this.customerId = customerId;
        this.threadId = threadId;
    }

    public enum State {
        PENDING, ONROUTE, DELIVERED
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

}