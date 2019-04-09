package uk.dioxic.mongotakeaway.web;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import uk.dioxic.mongotakeaway.domain.Customer;
import uk.dioxic.mongotakeaway.domain.Restaurant;
import uk.dioxic.mongotakeaway.repository.CustomerRepository;
import uk.dioxic.mongotakeaway.repository.RestaurantRepository;

import java.net.URI;

@Slf4j
@Component
public class RestaurantHandler extends AbstractRestHandler<Restaurant, ObjectId> {

    public RestaurantHandler(RestaurantRepository repository) {
        super(repository, Restaurant.class);
    }

    @Override
    ObjectId parseId(String id) {
        return new ObjectId(id);
    }

    @Override
    URI linkUri(Restaurant obj) {
        return URI.create("/restaurant/" + obj.getId().toHexString());
    }
}
