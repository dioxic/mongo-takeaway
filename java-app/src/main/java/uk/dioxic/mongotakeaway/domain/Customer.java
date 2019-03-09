package uk.dioxic.mongotakeaway.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Customer {

    @Id
    private String id;
    private String firstName;
    private String surnameName;

    public Customer() {
    }

    public Customer(String firstName, String lastName) {
        this.firstName = firstName;
        this.surnameName = lastName;
    }
}
