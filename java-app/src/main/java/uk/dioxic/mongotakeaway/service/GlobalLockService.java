package uk.dioxic.mongotakeaway.service;

import com.mongodb.client.result.DeleteResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.domain.GlobalLock;

import java.util.Objects;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Slf4j
@Service
public class GlobalLockService {

    private static final GlobalLock NULL_LOCK = new GlobalLock("NULL", 0);
    private ReactiveMongoTemplate reactiveTemplate;
    private Long pid;

    public GlobalLockService(ReactiveMongoTemplate reactiveTemplate) {
        this.reactiveTemplate = reactiveTemplate;
        pid = ProcessHandle.current().pid();
    }

    public boolean tryLock(String key) {
        Objects.requireNonNull(key);
        return reactiveTemplate.insert(new GlobalLock(key, pid))
//                .doOnError(e -> log.error(e.getMessage(),e))
//                .onErrorReturn(DuplicateKeyException.class, NULL_LOCK)
                .onErrorResume(DuplicateKeyException.class, e -> Mono.just(NULL_LOCK))
                .block() != NULL_LOCK;
    }

    public boolean unlock(String key) {
        return unlock(key,false);
    }

    public boolean unlock(String key, boolean force) {
        Objects.requireNonNull(key);
        Criteria criteria = where("_id").is(key);
        if (force)
            criteria = criteria.and("pid").is(pid);

        return reactiveTemplate.remove(query(criteria), GlobalLock.class)
                .map(DeleteResult::getDeletedCount)
                .filter(c -> c > 0)
                .block() != null;
    }

}
