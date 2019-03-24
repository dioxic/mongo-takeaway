package uk.dioxic.mongotakeaway.service;

import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.domain.CacheState;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

@Slf4j
@Service
public class CacheStateService {

    private ReactiveMongoTemplate reactiveTemplate;
    private Long pid;

    public CacheStateService(ReactiveMongoTemplate reactiveTemplate) {
        this.reactiveTemplate = reactiveTemplate;
        pid = ProcessHandle.current().pid();
    }

    public Boolean markDirty(String cache, boolean dirty) {
        return reactiveTemplate.upsert(query(where("id").is(cache)), update("dirty", dirty).set("pid", pid), CacheState.class)
                .map(UpdateResult::getModifiedCount)
                .filter(c -> c >= 1)
                .block() != null;
    }

    public Boolean isDirty(String cache) {
        return reactiveTemplate.findById(cache, CacheState.class)
                .map(CacheState::isDirty)
                .switchIfEmpty(Mono.just(Boolean.TRUE))
                .block();
    }

}
