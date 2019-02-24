package uk.dioxic.mongotakeaway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class TakeawayApplication {

	public static void main(String[] args) {
		SpringApplication.run(TakeawayApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> orderRoute(OrderHandler handler) {
		return route()
				.GET("/order/{id}", accept(APPLICATION_JSON), handler::getOrder)
				.GET("/order", accept(APPLICATION_JSON), handler::listOrders)
				.POST("/order", handler::createOrder)
				.build();
	}

}