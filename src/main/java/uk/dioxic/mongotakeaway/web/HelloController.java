package uk.dioxic.mongotakeaway.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public Mono<String> index() {
        return Mono.just("Greetings from Spring Boot!");
    }

}