package uk.dioxic.mongotakeaway.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Optional;

@Data
@Document
@NoArgsConstructor
public class Postcode {

    @Id
    private String postcode;

    @Field("loc")
    private GeoJson location;

    public Postcode(String postcode, GeoJson location) {
        this.postcode = postcode;
        this.location = location;
    }

    public Postcode(String postcode, double longitude, double latitude) {
        this(postcode, new GeoJson(longitude, latitude));
    }

    public Double getLongitude() {
        return getCoordinate(0);
    }

    public Double getLatitude() {
        return getCoordinate(1);
    }

    private Double getCoordinate(int i) {
        return Optional.ofNullable(location)
                .map(GeoJson::getCoordinates)
                .filter(list -> list.size() == 2)
                .map(c -> c.get(i))
                .orElse(null);
    }

    @Data
    @NoArgsConstructor
    static class GeoJson {
        String type = "Point";
        List<Double> coordinates;

        public GeoJson(double longitude, double latitude) {
            coordinates = List.of(longitude, latitude);
        }
    }

}
