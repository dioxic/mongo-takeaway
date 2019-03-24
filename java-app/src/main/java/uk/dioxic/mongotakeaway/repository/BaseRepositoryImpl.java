package uk.dioxic.mongotakeaway.repository;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.NonNull;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleReactiveMongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@NoRepositoryBean
public class BaseRepositoryImpl<T, ID extends Serializable>
        extends SimpleReactiveMongoRepository<T, ID>
        implements BaseRepository<T,ID> {

    private MongoEntityInformation<T, ID> entityInformation;
    private ReactiveMongoOperations mongoOperations;

    public BaseRepositoryImpl(@NonNull MongoEntityInformation<T, ID> entityInformation,
                              @NonNull ReactiveMongoOperations mongoOperations) {
        super(entityInformation, mongoOperations);
        this.entityInformation = entityInformation;
        this.mongoOperations = mongoOperations;
    }

    @Override
    public Mono<UpdateResult> updateById(ID id, Map<String, ?> setMap) {
        Update update = new Update();
        setMap.forEach(update::set);

        return mongoOperations.updateFirst(getIdQuery(id), update, entityInformation.getJavaType(), entityInformation.getCollectionName());
    }

    @Override
    public Mono<UpdateResult> updateByIdWithPredicate(ID id, Map<String, ?> setMap, Map<String, ?> predicate) {
        Update update = new Update();
        setMap.forEach(update::set);

        Criteria criteria = getIdCriteria(id);
        predicate.forEach((k,v) -> criteria.and(k).is(v));

        return mongoOperations.updateFirst(query(criteria), update, entityInformation.getJavaType(), entityInformation.getCollectionName());
    }

    @Override
    public Mono<DeleteResult> deleteByIdWithCount(ID id) {
        return mongoOperations
                .remove(getIdQuery(id), entityInformation.getJavaType(), entityInformation.getCollectionName());
    }

    private Query getIdQuery(Object id) {
        return new Query(getIdCriteria(id));
    }

    private Criteria getIdCriteria(Object id) {
        return where(entityInformation.getIdAttribute()).is(id);
    }
}
