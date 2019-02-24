package uk.dioxic.mongotakeaway.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.ChangeStreamService;
import uk.dioxic.mongotakeaway.util.DocumentUtil;
import uk.dioxic.mongotakeaway.config.GeneratorProperties;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Component("ReactiveWebSocketHandler")
public class ReactiveWebSocketHandler implements WebSocketHandler {

    private static final ObjectMapper json = new ObjectMapper();
    private static final Aggregation csPipeline = newAggregation(match(where("operationType").is("insert")
            .and("ns.coll").is("order")
            .and("fullDocument._id").exists(true)));

    private ChangeStreamService changeStreamService;
    private GeneratorProperties properties;

    public ReactiveWebSocketHandler(ChangeStreamService changeStreamService, GeneratorProperties properties) {
        this.changeStreamService = changeStreamService;
        this.properties = properties;
    }

    public void testFibonacciFluxSink() {
        Flux<Long> fibonacciGenerator = Flux.create(e -> {
            long current = 1, prev = 0;
            AtomicBoolean stop = new AtomicBoolean(false);
            e.onDispose(() -> {
                stop.set(true);
                System.out.println("******* Stop Received ****** ");
            });
            while (current > 0) {
                e.next(current);
                System.out.println("generated " + current);
                long next = current + prev;
                prev = current;
                current = next;
            }
            e.complete();
        });
        List<Long> fibonacciSeries = new LinkedList<>();
        fibonacciGenerator.take(50).subscribe(t -> {
            System.out.println("consuming " + t);
            fibonacciSeries.add(t);
        });
        System.out.println(fibonacciSeries);
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        webSocketSession.getAttributes().forEach((k, v) -> log.info("k: {}, v: {}", k, v));

        Random random = new Random();
        Integer customer = random.nextInt(properties.getCustomers());
        log.info("handling customer {}", customer);

        return webSocketSession.send(changeStreamService.subscribe(customer)
                .filter(cse -> customer.equals(Objects.requireNonNull(cse.getBody()).getCustomerId()))
                .map(ChangeStreamEvent::getRaw)
                .map(ChangeStreamDocument::getFullDocument)
                .map(DocumentUtil::toJson)
                .map(webSocketSession::textMessage))
                .and(webSocketSession.receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .doOnComplete(() -> changeStreamService.unsubscribe(customer))
                );
    }
}