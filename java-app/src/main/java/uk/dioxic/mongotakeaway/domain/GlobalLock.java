package uk.dioxic.mongotakeaway.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("locks")
@NoArgsConstructor
public class GlobalLock {

    @Id
    private String key;
//    @Indexed(expireAfterSeconds = 30)
    private LocalDateTime created;
    private long pid;

    public GlobalLock(String key, long pid) {
        this.key = key;
        this.created = LocalDateTime.now();
        this.pid = pid;
    }
}
