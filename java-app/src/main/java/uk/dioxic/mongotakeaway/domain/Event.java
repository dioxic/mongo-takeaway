package uk.dioxic.mongotakeaway.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@NoArgsConstructor
public class Event {

    private ObjectId id;
    private LocalDateTime eventTime;
    private String type;
    private String msg;

    public Event(String type, String msg) {
        this.type = type;
        this.msg = msg;
        this.eventTime = LocalDateTime.now();
    }
}
