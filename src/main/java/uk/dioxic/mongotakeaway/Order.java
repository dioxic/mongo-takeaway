package uk.dioxic.mongotakeaway;

import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Data
@Document
public class Order {
    @Id
    private Long id;
    private Integer customerId;
    private State state;
    private LocalDateTime created;
    private LocalDateTime modified;

    public Order() {

    }

    public Order(Order order) {
        this.id = order.id;
        this.customerId = order.customerId;
        this.state = order.state;
        this.created = order.created;
        this.modified = order.modified;
    }

    public Order(Long id, Integer customerId) {
        this.id = id;
        this.customerId = customerId;
        this.state = State.PENDING;
        this.created = now();
        this.modified = now();
    }

    enum State {
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