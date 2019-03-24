package uk.dioxic.mongotakeaway.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import uk.dioxic.mongotakeaway.domain.Event;
import uk.dioxic.mongotakeaway.repository.EventRepository;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EventService {

    @Getter
    private final ApplicationEventPublisher publisher;
    private final Flux<ChangeStreamEvent<Event>> eventFlux;
    private final EventRepository repository;
    private final List<Runnable> listeners = new ArrayList<>();

    public EventService(Flux<ChangeStreamEvent<Event>> eventFlux,
                        ApplicationEventPublisher publisher,
                        EventRepository repository) {
        this.publisher = publisher;
        this.eventFlux = eventFlux;
        this.repository = repository;
    }

    @EventListener
    public void handleApplicationReady(ApplicationReadyEvent event) {
        log.info("watching for events");

        eventFlux
                .publishOn(Schedulers.elastic())
                .map(ChangeStreamEvent::getBody)
                .doOnNext(e -> log.info("event {} [{}] received", e.getId(), e.getType()))
                .map(this::applicationEventFactory)
                .subscribe(publisher::publishEvent);
    }

    public void publishEvent(Event event) {
        repository.save(event).subscribe(e -> log.info("published {}", e.toString()));
    }

    public void publishEvent(ApplicationEvent applicationEvent) {
        Event event = new Event(applicationEvent.getClass().getSimpleName(), null);
        publishEvent(event);
    }

    private ApplicationEvent applicationEventFactory(Event event) {
        try {
            Class<?> clazz = Class.forName("uk.dioxic.mongotakeaway.event." + event.getType());
            return (ApplicationEvent)clazz.getConstructor(Object.class).newInstance(event);

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
