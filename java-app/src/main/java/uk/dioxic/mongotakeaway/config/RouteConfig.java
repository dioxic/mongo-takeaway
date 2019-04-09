package uk.dioxic.mongotakeaway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import uk.dioxic.mongotakeaway.web.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromResource;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class RouteConfig {

    @Bean("orderRoute")
    public RouterFunction<ServerResponse> orderRoute(OrderHandler handler) {
        return route()
                .GET("/order/{id}", accept(APPLICATION_JSON), handler::get)
                .GET("/order", accept(APPLICATION_JSON), handler::list)
                .POST("/order", accept(APPLICATION_JSON), handler::create)
                .DELETE("/order/{id}", handler::delete)
                .PATCH("/order/{id}", handler::modify)
                .build();
    }

    @Bean("geoRoute")
    public RouterFunction<ServerResponse> geoRoute(GeoHandler handler) {
        return route()
                .GET("/geo", accept(APPLICATION_JSON), handler::calculateDistanceFromPostcode)
                .build();
    }

    @Bean("customerRoute")
    public RouterFunction<ServerResponse> customerRoute(CustomerHandler handler) {
        return route()
                .GET("/customer/{id}", accept(APPLICATION_JSON), handler::get)
                .GET("/customer", accept(APPLICATION_JSON), handler::list)
                .POST("/customer", accept(APPLICATION_JSON), handler::create)
                .DELETE("/customer/{id}", handler::delete)
                .PATCH("/customer/{id}", handler::modify)
                .build();
    }

    @Bean("restaurantRoute")
    public RouterFunction<ServerResponse> restaurantRoute(RestaurantHandler handler) {
        return route()
                .GET("/restaurant/{id}", accept(APPLICATION_JSON), handler::get)
                .GET("/restaurant", accept(APPLICATION_JSON), handler::list)
                .POST("/restaurant", accept(APPLICATION_JSON), handler::create)
                .DELETE("/restaurant/{id}", handler::delete)
                .PATCH("/restaurant/{id}", handler::modify)
                .build();
    }

    @Bean("menuRoute")
    public RouterFunction<ServerResponse> menuRoute(MenuHandler handler) {
        return route()
                .GET("/menu/{id}", accept(APPLICATION_JSON), handler::get)
                .GET("/menu", accept(APPLICATION_JSON), handler::list)
                .POST("/menu", accept(APPLICATION_JSON), handler::create)
                .DELETE("/menu/{id}", handler::delete)
                .PATCH("/menu/{id}", handler::modify)
                .build();
    }

    @Bean("defaultRoute")
    public RouterFunction<ServerResponse> defaultRoute() {
        return route()
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
