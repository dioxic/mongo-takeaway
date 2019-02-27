package uk.dioxic.mongotakeaway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.BodyInserters.fromResource;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class RouteConfig {

    @Bean
    RouterFunction<ServerResponse> orderRoute() {
        return route()
//                .GET("/order/{id}", accept(APPLICATION_JSON), handler::getOrder)
//                .GET("/order", accept(APPLICATION_JSON), handler::listOrders)
//                .POST("/order", handler::createOrder)
                .GET("/", req -> ok().body(fromResource(new ClassPathResource("static/client-websocket.html"))))
                .GET("/react", req -> ok().body(fromResource(new ClassPathResource("static/index.html"))))
                .build();
    }
}
