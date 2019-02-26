package uk.dioxic.mongotakeaway.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.Order;
import uk.dioxic.mongotakeaway.OrderRepository;

@RestController
public class OrderController {

    private OrderRepository repository;

    public OrderController(OrderRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Flux<Order> findAll() {
        return repository.findAll();
    }

    @GetMapping(path = "order/{id}", produces = "application/json")
    public Mono<Order> findById(@PathVariable long id) {
        return repository.findById(id);
    }

    @DeleteMapping(path = "order/{id}", produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> deleteById(@PathVariable long id) {
        return repository.deleteById(id);
    }

    @PostMapping(path = "order", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Order> save(@RequestBody Order order) {
        return repository.save(order);
    }
}
