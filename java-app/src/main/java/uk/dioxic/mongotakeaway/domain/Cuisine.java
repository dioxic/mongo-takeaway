package uk.dioxic.mongotakeaway.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Cuisine {

    @Id
    private String id;
}
