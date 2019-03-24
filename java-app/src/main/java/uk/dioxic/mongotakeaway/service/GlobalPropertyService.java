package uk.dioxic.mongotakeaway.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import uk.dioxic.mongotakeaway.domain.GlobalProperties;
import uk.dioxic.mongotakeaway.event.PropertiesChangedEvent;
import uk.dioxic.mongotakeaway.repository.PropertyRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GlobalPropertyService {

    @Getter
    private GlobalProperties properties;
    private final PropertyRepository repository;
    private final ApplicationEventPublisher publisher;
    private final Flux<ChangeStreamEvent<GlobalProperties>> propertiesFlux;
    private final List<Runnable> listeners = new ArrayList<>();

    public GlobalPropertyService(Flux<ChangeStreamEvent<GlobalProperties>> propertiesFlux,
                                 ApplicationEventPublisher publisher,
                                 PropertyRepository repository) {
        this.publisher = publisher;
        this.repository = repository;
        this.propertiesFlux = propertiesFlux;
    }

    @EventListener
    public void handleApplicationReady(ApplicationReadyEvent event) {
        log.info("reading global properties");
        properties = repository.findById("MAIN").block();
        log.info("props = {}", properties);
        publisher.publishEvent(new PropertiesChangedEvent(properties));

        propertiesFlux
                .publishOn(Schedulers.elastic())
                .subscribe(cse -> {
                    log.info("change to global properties {}", cse.getBody());
                    properties = cse.getBody();
                    publisher.publishEvent(new PropertiesChangedEvent(properties));
                });
    }

}
