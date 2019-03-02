package uk.dioxic.mongotakeaway.web;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.service.OrderService;
import uk.dioxic.mongotakeaway.domain.Customer;
import uk.dioxic.mongotakeaway.generator.CustomerGenerator;
import uk.dioxic.mongotakeaway.util.DocumentUtil;

@Slf4j
@Component
public class ReactiveWebSocketHandler implements WebSocketHandler {

    private OrderService orderService;
    private CustomerGenerator customerGenerator;

    public ReactiveWebSocketHandler(OrderService orderService, CustomerGenerator customerGenerator) {
        this.orderService = orderService;
        this.customerGenerator = customerGenerator;
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        webSocketSession.getAttributes().forEach((k, v) -> log.info("k: {}, v: {}", k, v));

        Customer customer = customerGenerator.getRandomGeneratedCustomer();
        log.info("handling customer {}", customer);

        return webSocketSession.send(orderService.subscribe(customer)
                .map(ChangeStreamEvent::getRaw)
                .map(ChangeStreamDocument::getFullDocument)
                .map(DocumentUtil::toJson)
                .map(webSocketSession::textMessage))
                .and(webSocketSession.receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .doOnComplete(() -> orderService.unsubscribe(customer))
                );
    }
}