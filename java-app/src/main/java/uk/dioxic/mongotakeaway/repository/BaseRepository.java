package uk.dioxic.mongotakeaway.repository;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Mono;

import java.util.Map;

@NoRepositoryBean
public interface ExtendedRepository<T, ID> extends ReactiveMongoRepository<T, ID> {

    Mono<UpdateResult> updateById(ID id, Map<String, ?> setMap);

    Mono<DeleteResult> deleteByIdWithCount(ID id);

    Mono<UpdateResult> updateByIdWithPredicate(ID id, Map<String, ?> setMap, Map<String, ?> predicate);

}
