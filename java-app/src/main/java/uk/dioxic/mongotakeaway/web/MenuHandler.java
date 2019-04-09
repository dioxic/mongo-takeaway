package uk.dioxic.mongotakeaway.web;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.domain.Menu;
import uk.dioxic.mongotakeaway.repository.MenuRepository;
import uk.dioxic.mongotakeaway.repository.RestaurantRepository;

import java.net.URI;

@Slf4j
@Component
public class MenuHandler extends AbstractRestHandler<Menu, ObjectId> {

    private final MenuRepository repository;

    public MenuHandler(@NonNull MenuRepository repository) {
        super(repository, Menu.class);
        this.repository = repository;
    }

    @Override
    ObjectId parseId(String id) {
        return new ObjectId(id);
    }

    @Override
    URI linkUri(Menu obj) {
        return URI.create("/menu/" + obj.getId().toHexString());
    }

    @Override
    public Mono<ServerResponse> list(ServerRequest request) {
        log.info("handling get request for orders");
        return defaultReadResponse(request.queryParam("restaurantId")
                .map(ObjectId::new)
                .map(repository::findByRestaurantId)
                .orElseGet(repository::findAll));
    }
}
