package uk.dioxic.mongotakeaway.repository;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface OrderRepositoryEx<ID> {

    Mono<UpdateResult> updateById(ID id, Map<String, ? extends Object> setMap);

    Mono<DeleteResult> deleteByIdWithResults(ID id);

}
