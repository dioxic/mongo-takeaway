package uk.dioxic.mongotakeaway.repository;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface OrderRepositoryEx<ID> {

    Mono<UpdateResult> updateById(ID id, Map<String, ?> setMap);

    Mono<DeleteResult> deleteByIdWithResults(ID id);

    Mono<UpdateResult> updateByIdWithPredicate(ID id, Map<String, ?> setMap, Map<String, ?> predicate);

}
