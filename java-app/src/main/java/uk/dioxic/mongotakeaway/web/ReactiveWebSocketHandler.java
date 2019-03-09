package uk.dioxic.mongotakeaway.web;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.domain.Customer;
import uk.dioxic.mongotakeaway.domain.Order;
import uk.dioxic.mongotakeaway.generator.CustomerGenerator;
import uk.dioxic.mongotakeaway.service.ChangeStreamSubscriber;
import uk.dioxic.mongotakeaway.util.DocumentUtil;

@Slf4j
@Component
public class ReactiveWebSocketHandler implements WebSocketHandler {

    private CustomerGenerator customerGenerator;
    private ChangeStreamSubscriber<Order, ObjectId> subscriber;

    public ReactiveWebSocketHandler(@Qualifier("orderByCustomerIdSubscriber") ChangeStreamSubscriber<Order, ObjectId> subscriber,
                                    CustomerGenerator customerGenerator) {
        this.subscriber = subscriber;
        this.customerGenerator = customerGenerator;
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        webSocketSession.getAttributes().forEach((k, v) -> log.info("k: {}, v: {}", k, v));

        Customer customer = customerGenerator.getRandomGeneratedCustomer();
        log.info("handling customer {}", customer);

        return webSocketSession.send(subscriber.subscribe(new ObjectId(customer.getId()))
                .map(ChangeStreamEvent::getRaw)
                .map(ChangeStreamDocument::getFullDocument)
                .map(DocumentUtil::toJson)
                .map(webSocketSession::textMessage))
                .doOnError(e -> log.error(e.getMessage(), e))
                .and(webSocketSession.receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .doOnComplete(() -> subscriber.unsubscribe(new ObjectId(customer.getId())))
                );
    }
}