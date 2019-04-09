package uk.dioxic.mongotakeaway.repository;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;
import uk.dioxic.mongotakeaway.domain.Postcode;
import uk.dioxic.mongotakeaway.domain.Restaurant;

@Repository
public interface RestaurantRepository extends BaseRepository<Restaurant, ObjectId> {

}
