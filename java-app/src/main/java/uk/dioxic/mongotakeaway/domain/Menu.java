package uk.dioxic.mongotakeaway.domain;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
public class Menu {

    @Id
    private ObjectId id;
    private ObjectId restaurantId;
    private List<String> categories;

    class Category {
        private String name;
        private int order;
        private List<MenuItem> items;
    }
}
