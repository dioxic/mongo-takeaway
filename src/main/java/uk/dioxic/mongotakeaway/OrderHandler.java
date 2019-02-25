package uk.dioxic.mongotakeaway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@SuppressWarnings("ALL")
@Slf4j
@Component
public class OrderHandler {

    private OrderRepository repository;

    public OrderHandler(OrderRepository repository) {
        this.repository = repository;
    }

    public Mono<ServerResponse> listOrders(ServerRequest request) {
        return ok()
                .contentType(APPLICATION_JSON)
                .body(request.queryParam("customerId")
                        .map(Integer::valueOf)
                        .map(repository::findByCustomerId)
                        .orElseGet(repository::findAll), Order.class)
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).build().block())
            .switchIfEmpty(notFound().build());
    }

    public Mono<ServerResponse> createOrder(ServerRequest request) {
        return request.bodyToMono(Order.class)
            .doOnNext(order -> log.info("create order {}", order))
            .flatMap(repository::save)
            .flatMap(order -> created(URI.create("/order/" + order.getId()))
                    .contentType(APPLICATION_JSON)
                    .body(fromObject(order)))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).build().block())
            .switchIfEmpty(notFound().build());
    }

    public Mono<ServerResponse> getOrder(ServerRequest request) {
        return ok()
            .contentType(APPLICATION_JSON)
            .body(Mono.justOrEmpty(request.pathVariable("id"))
                    .map(Long::valueOf)
                    .flatMap(repository::findById)
                    .log(), Order.class)
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).build().block())
            .switchIfEmpty(notFound().build());
    }

    public Mono<ServerResponse> deleteOrder(ServerRequest request) {
        return accepted()
            .contentType(APPLICATION_JSON)
            .build(Mono.justOrEmpty(request.pathVariable("id"))
                    .map(Long::valueOf)
                    .flatMap(repository::deleteById)
            )
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).build().block())
            .switchIfEmpty(notFound().build());
    }

}
