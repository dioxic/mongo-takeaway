package uk.dioxic.mongotakeaway.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.dioxic.mongotakeaway.config.ChangeStreamProperties;
import uk.dioxic.mongotakeaway.config.GeneratorProperties;

@Document
@Accessors(fluent = true)
@Data
public class GlobalProperties {

    @Id
    private String id = "MAIN";
    private GeneratorProperties generator;
    private ChangeStreamProperties changeStream;
}
