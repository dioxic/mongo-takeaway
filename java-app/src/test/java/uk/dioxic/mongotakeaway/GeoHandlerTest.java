package uk.dioxic.mongotakeaway;

import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.dioxic.mongotakeaway.config.RouteConfig;
import uk.dioxic.mongotakeaway.domain.Order;
import uk.dioxic.mongotakeaway.service.GeoService;
import uk.dioxic.mongotakeaway.web.GeoHandler;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(SpringExtension.class)
@DisplayName("web endpoint tests")
@WebFluxTest
class GeoHandlerTest {

	private WebTestClient webTestClient;

	@MockBean
	private GeoService geoService;

	@BeforeEach
	void setUp() {
		webTestClient = WebTestClient.bindToRouterFunction(new RouteConfig().geoRoute(new GeoHandler(geoService))).build();
	}

	@Test
	void calculateDistanceFromPostcode() {
		when(geoService.distanceInMiles(anyString(), anyString()))
				.thenReturn(Mono.just(100d));

		webTestClient.get().uri("/geo?postcode1={code1}&postcode2={code2}", "ME", "DN")
				.exchange()
				.expectStatus().isOk()
				.expectBody(Double.class)
				.isEqualTo(100d);

		verify(geoService).distanceInMiles(anyString(), anyString());
	}

}
