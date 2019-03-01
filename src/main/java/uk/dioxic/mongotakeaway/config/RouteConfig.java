package uk.dioxic.mongotakeaway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import uk.dioxic.mongotakeaway.web.OrderHandler;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromResource;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class RouteConfig {

    private OrderHandler orderHandler;

    public RouteConfig(OrderHandler orderHandler) {
        this.orderHandler = orderHandler;
    }

    @Bean("orderRoute")
    public RouterFunction<ServerResponse> orderRoute() {
        return route()
                .GET("/order/{id}", accept(APPLICATION_JSON), orderHandler::getOrder)
                .GET("/order", accept(APPLICATION_JSON), orderHandler::listOrders)
                .POST("/order", orderHandler::createOrder)
                .DELETE("/order/{id}", orderHandler::deleteOrder)
                .PATCH("/order/{id}", orderHandler::modifyOrder)
                .GET("/", req -> ok().body(fromResource(new ClassPathResource("static/client-websocket.html"))))
                .GET("/react", req -> ok().body(fromResource(new ClassPathResource("static/index.html"))))
                .build();
    }
}
