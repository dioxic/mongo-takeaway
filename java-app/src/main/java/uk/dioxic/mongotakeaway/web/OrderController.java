package uk.dioxic.mongotakeaway.web;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.domain.Order;
import uk.dioxic.mongotakeaway.repository.OrderRepository;

import java.util.Map;

@Slf4j
//@RestController
public class OrderController {

    private OrderRepository repository;

    public OrderController(OrderRepository repository) {
        this.repository = repository;
    }

    @GetMapping(path = "/order", produces = "application/json")
    public Flux<Order> findAll() {
        return repository.findAll();
    }

    @GetMapping(path = "/order/{oid}", produces = "application/json")
    public Mono<Order> findById(@PathVariable String oid) {
        log.info("finding stuff for {}", oid);
        return repository.findById(new ObjectId(oid));
    }

    @DeleteMapping(path = "/order/{oid}", produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> deleteById(@PathVariable String oid) {
        return repository.deleteById(new ObjectId(oid));
    }

    @PostMapping(path = "/order", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Order> save(@RequestBody Order order) {
        log.info("save {}", order);
        return repository.save(order);
    }

    @PatchMapping(path = "/order/{oid}", produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> patch(@PathVariable String oid, @RequestParam Order.State state) {
        return repository.updateById(new ObjectId(oid), Map.of("state", state))
                .filter(result -> result.getModifiedCount() < 1)
                .then(Mono.empty());
    }
}
