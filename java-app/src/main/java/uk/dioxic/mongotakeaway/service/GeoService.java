package uk.dioxic.mongotakeaway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.domain.Postcode;
import uk.dioxic.mongotakeaway.repository.PostcodeRepository;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoService {

    private final PostcodeRepository repository;

    public Mono<Double> distance(String postcode1, String postcode2) {
        Objects.requireNonNull(postcode1);
        Objects.requireNonNull(postcode2);

        Consumer<List<Postcode>> logIfNotFound = postcodes -> {
            if (postcodes.size() != 2) {
                log.warn("postcode not found [{},{}]", postcode1, postcode2);
            }
        };

        return repository.findAllById(List.of(postcode1, postcode2))
                .collectList()
                .doOnNext(logIfNotFound)
                .filter(list -> list.size() == 2)
                .map(list -> distance(list.get(0), list.get(1)));
    }

    public Mono<Double> distanceInMiles(String postcode1, String postcode2) {
        return distance(postcode1, postcode2)
                .map(GeoService::metersToMiles);
    }

    private static double metersToMiles(double meters) {
        return meters / 1609.344d;
    }

    public static Double distance(Postcode postcode1, Postcode postcode2) {
        return distance(postcode1.getLatitude(), postcode2.getLatitude(), postcode1.getLongitude(), postcode2.getLongitude(), 0, 0);
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Meters
     */
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }
}
