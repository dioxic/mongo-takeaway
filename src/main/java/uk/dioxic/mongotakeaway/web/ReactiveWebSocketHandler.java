package uk.dioxic.mongotakeaway.web;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.ChangeStreamService;
import uk.dioxic.mongotakeaway.config.GeneratorProperties;
import uk.dioxic.mongotakeaway.util.DocumentUtil;

import java.util.Random;

@Slf4j
@Component
public class ReactiveWebSocketHandler implements WebSocketHandler {

    private ChangeStreamService changeStreamService;
    private GeneratorProperties properties;

    public ReactiveWebSocketHandler(ChangeStreamService changeStreamService, GeneratorProperties properties) {
        this.changeStreamService = changeStreamService;
        this.properties = properties;
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        webSocketSession.getAttributes().forEach((k, v) -> log.info("k: {}, v: {}", k, v));

        Random random = new Random();
        Integer customer = random.nextInt(properties.getCustomers());
        log.info("handling customer {}", customer);

        return webSocketSession.send(changeStreamService.subscribe(customer)
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