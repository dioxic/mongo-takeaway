package uk.dioxic.mongotakeaway.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.domain.Order;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, ObjectId>, OrderRepositoryEx<ObjectId> {

    Flux<Order> findByCustomerId(final Integer customerId);

}
