package uk.dioxic.mongotakeaway.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import uk.dioxic.mongotakeaway.domain.Event;
import uk.dioxic.mongotakeaway.domain.NodeInfo;
import uk.dioxic.mongotakeaway.repository.EventRepository;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.ChangeStreamOptions.builder;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    @Getter
    private final ReactiveMongoTemplate reactiveTemplate;
    private final ApplicationEventPublisher publisher;
    private final EventRepository repository;
    private final NodeInfo nodeInfo;
    private final List<Runnable> listeners = new ArrayList<>();

    @EventListener
    public void handleApplicationReady(ApplicationReadyEvent event) {
        log.info("watching for events");

        reactiveTemplate.changeStream(ChangeStreamSubscriber.getCollectionName(Event.class),
                builder()
                        .filter(newAggregation(match(where("operationType").in("insert"))))
                        .build(),
                Event.class)
//                .publishOn(Schedulers.elastic())
                .map(ChangeStreamEvent::getBody)
                .doOnNext(e -> log.info("received {} ", e))
                .map(Event::getApplicationEvent)
                .subscribe(publisher::publishEvent);
    }

    public void publishEvent(Class<? extends ApplicationEvent> eventType) {
        publishEvent(new Event(eventType, nodeInfo));
    }

    public void publishEvent(Event event) {
        repository.save(event).subscribe(e -> log.info("published {}", e.toString()));
    }

}
