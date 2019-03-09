package uk.dioxic.mongotakeaway.repository;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.domain.Order;

import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class OrderRepositoryExImpl<ID> implements OrderRepositoryEx<ID> {

    @Autowired
    private ReactiveMongoTemplate template;

    @Autowired
    private ReactiveMongoOperations mongoOperations;

    @Override
    public Mono<UpdateResult> updateById(ID id, Map<String, ?> setMap) {
        Update update = new Update();
        setMap.forEach(update::set);

        return template.updateFirst(query(where("_id").is(id)), update, Order.class);
    }

    @Override
    public Mono<UpdateResult> updateByIdWithPredicate(ID id, Map<String, ?> setMap, Map<String, ?> predicate) {
        Update update = new Update();
        setMap.forEach(update::set);

        Criteria criteria = where("_id").is(id);
        predicate.forEach((k,v) -> criteria.and(k).is(v));

        return template.updateFirst(query(criteria), update, Order.class);
    }

    @Override
    public Mono<DeleteResult> deleteByIdWithResults(ID id) {
        return template.remove(query(where("_id").is(id)), Order.class);
    }
}
