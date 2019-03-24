package uk.dioxic.mongotakeaway.web;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.domain.Order;
import uk.dioxic.mongotakeaway.repository.OrderRepository;

import java.net.URI;

@Slf4j
@Component
public class OrderHandler extends AbstractRestHandler<Order, ObjectId>{

    private OrderRepository repository;

    public OrderHandler(OrderRepository repository) {
        super(repository, Order.class);
        this.repository = repository;
    }

    @Override
    ObjectId parseId(String id) {
        return new ObjectId(id);
    }

    @Override
    URI linkUri(Order obj) {
        return URI.create("/order/" + obj.getId().toHexString());
    }

    @Override
    public Mono<ServerResponse> list(ServerRequest request) {
        log.info("handling get request for orders");
        return defaultReadResponse(request.queryParam("customerId")
                .map(ObjectId::new)
                .map(repository::findByCustomerId)
                .orElseGet(repository::findAll));
    }

}
