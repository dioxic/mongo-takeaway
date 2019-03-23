package uk.dioxic.mongotakeaway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import uk.dioxic.mongotakeaway.web.OrderHandler;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromResource;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class RouteConfig {

    @Bean("orderRoute")
    public RouterFunction<ServerResponse> orderRoute(OrderHandler orderHandler) {
        return route()
                .GET("/order/{id}", accept(APPLICATION_JSON), orderHandler::getOrder)
                .GET("/order", accept(APPLICATION_JSON), orderHandler::listOrders)
                .POST("/order", accept(APPLICATION_JSON), orderHandler::createOrder)
                .DELETE("/order/{id}", orderHandler::deleteOrder)
                .PATCH("/order/{id}", orderHandler::modifyOrder)
                .GET("/", req -> ok().body(fromResource(new ClassPathResource("static/client-websocket.html"))))
                .GET("/react", req -> ok().body(fromResource(new ClassPathResource("static/index.html"))))
                .build();
    }

    @Bean
    CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("http://localhost:3000");
        corsConfig.setAllowedMethods(List.of("*"));
        corsConfig.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
