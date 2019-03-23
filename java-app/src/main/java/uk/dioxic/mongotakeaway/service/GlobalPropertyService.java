package uk.dioxic.mongotakeaway.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import uk.dioxic.mongotakeaway.domain.GlobalProperties;
import uk.dioxic.mongotakeaway.repository.PropertyRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GlobalPropertyService implements ApplicationListener<ApplicationReadyEvent> {

    @Getter
    private GlobalProperties properties;
    private final ChangeStreamSubscriber<GlobalProperties, String> changeStream;
    private final PropertyRepository repository;
    private final List<Runnable> listeners = new ArrayList<>();

    public GlobalPropertyService(@Qualifier("globalProperties") ChangeStreamSubscriber<GlobalProperties, String> changeStream,
                                 PropertyRepository repository) {
        this.changeStream = changeStream;
        this.repository = repository;
    }

    public void addListener(Runnable runnable) {
        listeners.add(runnable);
    }

    public void removeListener(Runnable runnable) {
        listeners.remove(runnable);
    }


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("reading global properties");
        properties = repository.findById("MAIN").block();
        log.info("props = {}", properties);
        changeStream.subscribe()
                .subscribe(cse -> {
                    log.info("change to global properties {}", cse.getBody());
                    properties = cse.getBody();
                    listeners.forEach(Runnable::run);
                });
    }
}
