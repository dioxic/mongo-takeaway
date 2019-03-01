package uk.dioxic.mongotakeaway.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import uk.dioxic.mongotakeaway.Order;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, String>, OrderRepositoryEx<String> {

    Flux<Order> findByCustomerId(final Integer customerId);

}
