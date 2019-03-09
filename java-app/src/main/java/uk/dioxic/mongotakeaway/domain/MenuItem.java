package uk.dioxic.mongotakeaway.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document
public class MenuItem {

    @Id
    private String id;
    private String name;
    private String description;
    private BigDecimal price;

    public MenuItem() {
    }

    public MenuItem(String name, String description, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }
}
