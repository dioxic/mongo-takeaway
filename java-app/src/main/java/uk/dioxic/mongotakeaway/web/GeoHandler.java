package uk.dioxic.mongotakeaway.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import uk.dioxic.mongotakeaway.domain.Customer;
import uk.dioxic.mongotakeaway.repository.CustomerRepository;
import uk.dioxic.mongotakeaway.service.GeoService;

import java.net.URI;
import java.util.Optional;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeoHandler {

    private final GeoService geoService;

    public Mono<ServerResponse> calculateDistanceFromPostcode(ServerRequest request) {
        Optional<String> postcode1 = request.queryParam("postcode1");
        Optional<String> postcode2 = request.queryParam("postcode2");

        if (postcode1.isEmpty() || postcode2.isEmpty()) {
            return ServerResponse.badRequest().build();
        }

        return geoService.distanceInMiles(postcode1.get(), postcode2.get())
                .doOnNext(dist -> log.info("{}", dist))
                .flatMap(dist -> ServerResponse.ok().body(fromObject(dist)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
