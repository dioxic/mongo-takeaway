package uk.dioxic.mongotakeaway.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import uk.dioxic.mongotakeaway.domain.GlobalProperties;
import uk.dioxic.mongotakeaway.domain.Order;

@Repository
public interface PropertyRepository extends ReactiveMongoRepository<GlobalProperties, String> {

}
