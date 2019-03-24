package uk.dioxic.mongotakeaway.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import uk.dioxic.mongotakeaway.domain.Event;
import uk.dioxic.mongotakeaway.domain.GlobalProperties;

@Repository
public interface EventRepository extends ReactiveMongoRepository<Event, ObjectId> {

}
