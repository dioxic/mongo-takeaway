package uk.dioxic.mongotakeaway.web;

import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.Order;
import uk.dioxic.mongotakeaway.repository.OrderRepository;

import java.net.URI;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@SuppressWarnings("ALL")
@Slf4j
//@Deprecated
@Component
public class OrderHandler {

    private OrderRepository repository;

    public OrderHandler(OrderRepository repository) {
        this.repository = repository;
    }

    public Mono<ServerResponse> listOrders(ServerRequest request) {
        log.info("handling get request for orders");
        return ok()
            .contentType(APPLICATION_JSON)
            .body(request.queryParam("customerId")
                .map(Integer::valueOf)
                .map(repository::findByCustomerId)
                .orElseGet(repository::findAll), Order.class)
            .doOnError(e -> log.error(e.getMessage(), e))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).build().block())
            .switchIfEmpty(notFound().build());
    }

    public Mono<ServerResponse> createOrder(ServerRequest request) {
        return request.bodyToMono(Order.class)
            .doOnNext(order -> log.info("handling create request for {}", order))
            .flatMap(repository::save)
            .flatMap(order -> created(URI.create("/order/" + order.getId()))
                .contentType(APPLICATION_JSON)
                .body(fromObject(order)))
            .doOnError(e -> log.error(e.getMessage(), e))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).build().block())
            .switchIfEmpty(notFound().build());
    }

    public Mono<ServerResponse> getOrder(ServerRequest request) {
        return Mono.justOrEmpty(request.pathVariable("id"))
            .doOnNext(id -> log.debug("handling get request for id:{}", id))
            .flatMap(repository::findById)
            .flatMap(order -> ok()
                .contentType(APPLICATION_JSON)
                .body(fromObject(order))
            )
            .doOnError(e -> log.error(e.getMessage(), e))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).build().block())
            .switchIfEmpty(notFound().build());
    }

    public Mono<ServerResponse> deleteOrder(ServerRequest request) {
        return Mono.justOrEmpty(request.pathVariable("id"))
            .doOnNext(id -> log.debug("handling delete request for id:{}", id))
            .flatMap(id -> repository.deleteByIdWithResults(id))
            .map(res -> res.getDeletedCount())
            .filter(count -> count > 0)
            .flatMap(c -> accepted()
                .contentType(APPLICATION_JSON)
                .build())
            .doOnError(e -> log.error(e.getMessage(), e))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).build().block())
            .switchIfEmpty(notFound().build());
    }

    public Mono<ServerResponse> modifyOrder(ServerRequest request) {
        log.info(request.queryParams().toString());
         return repository.updateById(
                request.pathVariable("id"),
                request.queryParams().toSingleValueMap()
            )
            .map(UpdateResult::getModifiedCount)
            .filter(count -> count > 0)
            .flatMap(c -> accepted()
            .contentType(APPLICATION_JSON)
            .build())
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).build().block())
            .switchIfEmpty(notFound().build());
    }

}
