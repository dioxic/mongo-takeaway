package uk.dioxic.mongotakeaway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.dioxic.mongotakeaway.domain.NodeInfo;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class NodeStatusService {

    private final ReactiveMongoTemplate template;
    private final NodeInfo nodeInfo;

    @Async
    @EventListener
    public void handleApplicationReady(ApplicationReadyEvent event) {
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(this::heartbeatUpdate,
                        0,
                        5,
                        TimeUnit.SECONDS);
    }

    private void heartbeatUpdate() {
        log.trace("updating heartbeat status");
        nodeInfo.heartbeat(template).block();
    }

    @PreDestroy
    public void handleApplicationShutdown() {
        template.remove(nodeInfo).block();
    }

}
