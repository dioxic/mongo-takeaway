package uk.dioxic.mongotakeaway.repository;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import uk.dioxic.mongotakeaway.domain.Order;

@Repository
public interface OrderRepository extends BaseRepository<Order, ObjectId> {

    Flux<Order> findByCustomerId(final ObjectId customerId);

}
