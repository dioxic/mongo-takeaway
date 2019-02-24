package uk.dioxic.mongotakeaway;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Document
public class Order {
    @Id
    private Long id;
    private Integer customerId;
    private State state;
    private LocalDateTime created;
    private LocalDateTime modified;

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