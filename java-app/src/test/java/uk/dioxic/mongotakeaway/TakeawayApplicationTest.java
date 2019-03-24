package uk.dioxic.mongotakeaway;

import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
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
import uk.dioxic.mongotakeaway.repository.OrderRepository;
import uk.dioxic.mongotakeaway.web.OrderHandler;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(SpringExtension.class)
@DisplayName("web endpoint tests")
@WebFluxTest
class TakeawayApplicationTest {

//	@Autowired
	private WebTestClient webTestClient;

//	@Autowired
//	private ApplicationContext context;
//
//	@Autowired
//	@Qualifier("orderRoute")
//	private RouterFunction<ServerResponse> orderRoute;

	@MockBean
	private OrderRepository repository;

	@BeforeEach
	void setUp() {
		//    supposed to bind the router functions but didn't work
		webTestClient = WebTestClient.bindToRouterFunction(new RouteConfig().orderRoute(new OrderHandler(repository))).build();
	}

	@Test
	@Disabled
	void hello() {
		webTestClient.get().uri("/hello")
//				.accept(MediaType.APPLICATION_JSON_UTF8)
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.isEqualTo("Greetings from Spring Boot!");
	}

	@Test
	public void findAll() {
		when(repository.findAll())
				.thenReturn(Flux.range(0, 5)
					.map(i -> new Order())
					.doOnNext(order -> order.setId(ObjectId.get())
				));

		webTestClient.get().uri("/order")
//				.accept(MediaType.APPLICATION_JSON_UTF8)
				.exchange()
				.expectStatus().isOk()
				.expectBodyList(Order.class)
					.hasSize(5)
					.value(orders -> orders.forEach(order -> log.info(order.toString())));

		verify(repository).findAll();
	}

	@Test
	public void findById_ok() {
		Order order = new Order();
		order.setId(ObjectId.get());

		when(repository.findById(Mockito.<ObjectId>any()))
				.thenReturn(Mono.just(order));

		webTestClient.get().uri("/order/"+ order.getId())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody(Order.class)
				.isEqualTo(order);

		verify(repository).findById(order.getId());
	}

	@Test
	public void findById_notFound() {
		Order order = new Order();
		order.setId(ObjectId.get());

		when(repository.findById(Mockito.<ObjectId>any()))
				.thenReturn(Mono.empty());

		webTestClient.get().uri("/order/"+ order.getId())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isNotFound();

		verify(repository).findById(order.getId());
	}

	@Test
	public void save() {
		Order order = new Order();
		Order orderWithId = new Order(order);
		orderWithId.setId(ObjectId.get());

		when(repository.save(order))
				.thenReturn(Mono.just(orderWithId));

		webTestClient.post().uri("/order")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.body(Mono.just(order), Order.class)
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Order.class)
				.isEqualTo(orderWithId);

		verify(repository).save(order);
	}

	@Test
	public void delete_exists() {
		ObjectId oid = ObjectId.get();
		when(repository.deleteByIdWithCount(eq(oid)))
				.thenReturn(Mono.just(DeleteResult.acknowledged(1)));

		webTestClient.delete().uri("/order/" + oid)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isAccepted();

		verify(repository).deleteByIdWithCount(eq(oid));
	}

	@Test
	public void delete_notfound() {
		ObjectId oid = ObjectId.get();
		when(repository.deleteByIdWithCount(eq(oid)))
				.thenReturn(Mono.just(DeleteResult.acknowledged(0)));

		webTestClient.delete().uri("/order/" + oid)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isNotFound();

		verify(repository).deleteByIdWithCount(eq(oid));
	}

}
