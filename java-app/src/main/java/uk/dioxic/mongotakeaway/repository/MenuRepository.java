package uk.dioxic.mongotakeaway.repository;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import uk.dioxic.mongotakeaway.domain.Menu;
import uk.dioxic.mongotakeaway.domain.Restaurant;

@Repository
public interface MenuRepository extends BaseRepository<Menu, ObjectId> {

    Flux<Menu> findByRestaurantId(ObjectId restaurantId);

}
