package uk.dioxic.mongotakeaway.web;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.repository.BaseRepository;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Slf4j
@AllArgsConstructor
public abstract class AbstractRestHandler<T, ID> {

    private final @NonNull BaseRepository<T, ID> repository;
    private final @NonNull Class<T> targetClass;

    abstract ID parseId(String id);

    abstract URI linkUri(T obj);

    public Mono<ServerResponse> list(ServerRequest request) {
        log.info("handling get request for orders");
        return defaultReadResponse(repository.findAll());
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        return defaultCreateResponse(request.bodyToMono(targetClass)
                .doOnNext(obj -> log.info("handling create request for {}", obj))
                .flatMap(repository::save));
    }

    public Mono<ServerResponse> get(ServerRequest request) {
        return defaultReadResponse(Mono.justOrEmpty(id(request))
                .doOnNext(id -> log.info("handling get request for {} [id={}]", targetClass.getSimpleName(), id))
                .map(this::parseId)
                .flatMap(repository::findById));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        return defaultModifyResponse(Mono.justOrEmpty(id(request))
                .doOnNext(id -> log.info("handling delete request for {} [id={}]", targetClass.getSimpleName(), id))
                .map(this::parseId)
                .flatMap(repository::deleteByIdWithCount)
                .map(DeleteResult::getDeletedCount));
    }

    public Mono<ServerResponse> modify(ServerRequest request) {
        log.info(request.queryParams().toString());
        return defaultModifyResponse(Mono.justOrEmpty(id(request))
                .doOnNext(id -> log.info("handling modify request for {} [id={}]", targetClass.getSimpleName(), id))
                .map(this::parseId)
                .flatMap(id -> repository.updateById(id,
                        request.queryParams().toSingleValueMap()))
                .map(UpdateResult::getModifiedCount)
        );
    }

    Mono<ServerResponse> defaultReadResponse(Mono<T> items) {
        return items
                .flatMap(o -> ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromObject(o)))
                .switchIfEmpty(notFound().build());
    }

    Mono<ServerResponse> defaultReadResponse(Flux<T> orders) {
        return ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(orders, targetClass);
    }

    Mono<ServerResponse> defaultModifyResponse(Publisher<Long> orders) {
        return Mono.from(orders)
                .filter(count -> count > 0)
                .flatMap(o -> accepted()
                        .contentType(APPLICATION_JSON)
                        .build())
                .doOnError(e -> log.error(e.getMessage(), e))
                .switchIfEmpty(notFound().build());
    }

    Mono<ServerResponse> defaultCreateResponse(Mono<T> items) {
        return items
                .flatMap(o -> created(linkUri(o))
                        .contentType(APPLICATION_JSON)
                        .body(fromObject(o)))
                .doOnError(e -> log.error(e.getMessage(), e));
    }

    static String id(ServerRequest r) {
        return r.pathVariable("id");
    }
}
