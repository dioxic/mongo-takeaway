package uk.dioxic.mongotakeaway.repository;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;
import uk.dioxic.mongotakeaway.domain.Event;

@Repository
public interface EventRepository extends BaseRepository<Event, ObjectId> {

}
