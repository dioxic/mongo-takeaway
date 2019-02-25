package uk.dioxic.mongotakeaway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import uk.dioxic.mongotakeaway.config.GeneratorProperties;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@SpringBootApplication
public class TakeawayApplication implements CommandLineRunner {

	@Autowired
	private GeneratorProperties properties;

	@Autowired
	private	OrderGenerator generator;

	public static void main(String[] args) {
		SpringApplication.run(TakeawayApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> orderRoute(OrderHandler handler) {
		return route()
				.GET("/order/{id}", accept(APPLICATION_JSON), handler::getOrder)
				.GET("/order", accept(APPLICATION_JSON), handler::listOrders)
				.POST("/order", handler::createOrder)
				.GET("/", req -> ok().body(fromResource(new ClassPathResource("static/client-websocket.html"))))
				.GET("/react", req -> ok().body(fromResource(new ClassPathResource("static/index.html"))))
				.build();
	}

	@Override
	public void run(String... args) {
		if (properties.getJobInterval() > 0) {
			Executors.newSingleThreadScheduledExecutor()
					.scheduleAtFixedRate(generator::scheduledJob,
							1,
							properties.getJobInterval(),
							TimeUnit.SECONDS);
		}
		if (properties.getRate() != 0) {
			Executors.newSingleThreadScheduledExecutor()
					.schedule(generator::generateJob,
							1,
							TimeUnit.SECONDS);
		}
	}
}
