package uk.dioxic.mongotakeaway.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

@Data
@Document
@RequiredArgsConstructor
public class NodeInfo {
    @Id
    private ObjectId id;
    private final String host;
    private final int port;
    private final long pid;

    public Mono<Void> heartbeat(ReactiveMongoTemplate template) {
        return template.upsert(query(where("host").is(host).and("port").is(port)),
                update("pid", pid).currentDate("lastHeartbeat"),
                NodeInfo.class)
                .then();
    }

}
