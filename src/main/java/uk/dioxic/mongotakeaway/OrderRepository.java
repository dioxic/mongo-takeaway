package uk.dioxic.mongotakeaway;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, Long> {

    Flux<Order> findByCustomerId(final Integer customerId);

    Mono<Order> save(final Mono<Order> order);
}
