package uk.dioxic.mongotakeaway.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import uk.dioxic.mongotakeaway.domain.AppSettings;
import uk.dioxic.mongotakeaway.event.PropertiesChangedEvent;
import uk.dioxic.mongotakeaway.repository.PropertyRepository;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.ChangeStreamOptions.builder;
import static uk.dioxic.mongotakeaway.service.ChangeStreamSubscriber.getCollectionName;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalPropertyService {

    @Getter
    private AppSettings settings;
    private final PropertyRepository repository;
    private final ApplicationEventPublisher publisher;
    private final ReactiveMongoTemplate reactiveTemplate;
    private final List<Runnable> listeners = new ArrayList<>();

    @EventListener
    public void handleApplicationReady(ApplicationReadyEvent event) {
        log.info("reading global settings");
        settings = repository.findById("MAIN").block();

        if (settings == null) {
            log.info("initialising global settings");
            settings = repository.save(new AppSettings()).block();
        }

        log.info("props = {}", settings);
        publisher.publishEvent(new PropertiesChangedEvent(settings));

        reactiveTemplate.changeStream(getCollectionName(AppSettings.class), builder().build(), AppSettings.class)
//                .publishOn(Schedulers.elastic())
                .map(ChangeStreamEvent::getBody)
                .doOnNext(body -> log.info("change to global settings {}", body))
                .map(PropertiesChangedEvent::new)
                .subscribe(publisher::publishEvent);
    }

}
