package uk.dioxic.mongotakeaway.web;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.domain.Order;
import uk.dioxic.mongotakeaway.repository.OrderRepository;

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
        log.info("handling get request for orders");
        return defaultReadResponse(request.queryParam("customerId")
                .map(Integer::valueOf)
                .map(repository::findByCustomerId)
                .orElseGet(repository::findAll));
    }

    public Mono<ServerResponse> createOrder(ServerRequest request) {
        return defaultCreateResponse(request.bodyToMono(Order.class)
                .doOnNext(order -> log.info("handling create request for {}", order))
                .flatMap(repository::save));
    }

    public Mono<ServerResponse> getOrder(ServerRequest request) {
        return defaultReadResponse(Mono.justOrEmpty(id(request))
                .doOnNext(id -> log.debug("handling get request for id:{}", id))
                .map(ObjectId::new)
                .flatMap(repository::findById));
    }

    public Mono<ServerResponse> deleteOrder(ServerRequest request) {
        return defaultModifyResponse(Mono.justOrEmpty(id(request))
                .doOnNext(id -> log.debug("handling delete request for id:{}", id))
                .flatMap(id -> repository.deleteByIdWithResults(new ObjectId(id)))
                .map(res -> res.getDeletedCount()));
    }

    public Mono<ServerResponse> modifyOrder(ServerRequest request) {
        log.info(request.queryParams().toString());
        return defaultModifyResponse(Mono.justOrEmpty(id(request))
                .doOnNext(order -> log.info("handling create request for {}", order))
                .map(ObjectId::new)
                .flatMap(id -> repository.updateById(id,
                    request.queryParams().toSingleValueMap()))
                .map(UpdateResult::getModifiedCount)
        );
    }

    private static Mono<ServerResponse> defaultReadResponse(Mono<Order> orders) {
        return orders
                .flatMap(o -> ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromObject(o)))
                .switchIfEmpty(notFound().build());
    }

    private static Mono<ServerResponse> defaultReadResponse(Flux<Order> orders) {
        return ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(orders, Order.class);
    }


    private static Mono<ServerResponse> defaultModifyResponse(Publisher<Long> orders) {
        return Mono.from(orders)
                .filter(count -> count > 0)
                .flatMap(o -> accepted()
                        .contentType(APPLICATION_JSON)
                        .build())
                .doOnError(e -> log.error(e.getMessage(), e))
                .switchIfEmpty(notFound().build());
    }

    private static Mono<ServerResponse> defaultCreateResponse(Mono<Order> orders) {
        return orders
                .flatMap(o -> created(URI.create("/order/" + o.getId().toHexString()))
                        .contentType(APPLICATION_JSON)
                        .body(fromObject(o)))
                .doOnError(e -> log.error(e.getMessage(), e));
    }

    private static String id(ServerRequest r) {
        return r.pathVariable("id");
    }

}
