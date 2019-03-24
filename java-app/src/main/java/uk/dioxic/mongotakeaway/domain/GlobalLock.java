package uk.dioxic.mongotakeaway.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@NoArgsConstructor
public class GlobalLock {

    @Id
    private String key;
    private LocalDateTime created;
    private long pid;

    public GlobalLock(String key, long pid) {
        this.key = key;
        this.created = LocalDateTime.now();
        this.pid = pid;
    }
}
