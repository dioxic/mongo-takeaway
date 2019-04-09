package uk.dioxic.mongotakeaway.domain;


import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
public class Restaurant {

    @Id
    private ObjectId id;
    private String displayName;
    private String shortName;
    private String description;
    private Address address;
    private byte[] logo;
    private List<String> deliveryAreas;
    private List<String> cuisineTags;
    private int rating;
    private int deliveryFee;
    private int minimumOrder;

}
