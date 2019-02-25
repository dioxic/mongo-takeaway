package uk.dioxic.mongotakeaway;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@Slf4j
@Disabled
@ExtendWith(SpringExtension.class)
@DisplayName("web endpoint tests")
@WebFluxTest
public class TakeawayApplicationTest {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private ApplicationContext context;

	@BeforeEach
	public void setUp()
	{
		webTestClient = WebTestClient.bindToApplicationContext(context).build();
	}

	@Test
	public void hello() {
		webTestClient.get().uri("/hello")
//				.accept(MediaType.APPLICATION_JSON_UTF8)
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.isEqualTo("Greetings from Spring Boot!");
	}

	@Test
	public void orders() {
		webTestClient.get().uri("/order")
//				.accept(MediaType.APPLICATION_JSON_UTF8)
				.exchange()
				.expectStatus().isOk();
	}

	@Test
	public void saveOrder() {

		Order order = new Order(0L, 99);

		webTestClient.post().uri("/order")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.body(Mono.just(order), Order.class)
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
					.jsonPath("$.id").isNotEmpty()
					.jsonPath("$.customerId").isEqualTo(99);

	}

}
