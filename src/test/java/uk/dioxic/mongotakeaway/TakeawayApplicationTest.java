package uk.dioxic.mongotakeaway;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@ExtendWith(SpringExtension.class)
@DisplayName("web endpoint tests")
@WebFluxTest
public class TakeawayApplicationTest {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private ApplicationContext context;

	@MockBean
	private OrderRepository repository;

	@BeforeEach
	public void setUp() {
		//    supposed to bind the router functions but didn't work
		//		webTestClient = WebTestClient.bindToApplicationContext(context).build();
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
	public void findAll() {
		Mockito.when(repository.findAll()).thenReturn(Flux.just(
				new Order(1L, 0),
				new Order(2L, 0),
				new Order(3L, 0)));

		webTestClient.get().uri("/order")
//				.accept(MediaType.APPLICATION_JSON_UTF8)
				.exchange()
				.expectStatus().isOk()
				.expectBodyList(Order.class)
					.hasSize(3)
					.value(orders -> orders.forEach(order -> log.info(order.toString())));

		Mockito.verify(repository).findAll();
	}

	@Test
	public void findById() {
		Order order = new Order(1L, 0);

		Mockito.when(repository.findById(order.getId()))
				.thenReturn(Mono.just(order));

		webTestClient.get().uri("/order/"+ order.getId())
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.exchange()
				.expectStatus().isOk()
				.expectBody(Order.class)
				.isEqualTo(order);

		Mockito.verify(repository).findById(order.getId());
	}

	@Test
	public void save() {

		Order order = new Order(null, 99);
		Order orderWithId = new Order(order);
		orderWithId.setId(1L);

		Mockito.when(repository.save(order))
				.thenReturn(Mono.just(orderWithId));

		webTestClient.post().uri("/order")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.body(Mono.just(order), Order.class)
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Order.class)
				.isEqualTo(orderWithId);

	}

	@Test
	public void delete() {

		Mockito.when(repository.deleteById(1L))
				.thenReturn(Mono.empty());

		webTestClient.delete().uri("/order/1")
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.exchange()
				.expectStatus().isAccepted();

	}

}
