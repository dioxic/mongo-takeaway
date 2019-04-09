package uk.dioxic.mongotakeaway.domain;

import lombok.Data;

@Data
public class Address {

    private String line1;
    private String line2;
    private String city;
    private Postcode postcode;
}
