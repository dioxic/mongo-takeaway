package uk.dioxic.mongotakeaway.domain;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class CacheState {

    private String id;
    private boolean dirty;

}
