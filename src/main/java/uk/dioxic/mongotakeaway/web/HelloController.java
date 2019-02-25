package uk.dioxic.mongotakeaway.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HelloController {

    @RequestMapping("/hello")
    public Mono<String> index() {
        return Mono.just("Greetings from Spring Boot!");
    }

}