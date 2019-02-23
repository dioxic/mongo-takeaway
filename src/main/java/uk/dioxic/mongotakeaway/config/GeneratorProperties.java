package uk.dioxic.mongotakeaway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("classpath:generator.properties")
@ConfigurationProperties(prefix = "generator")
public class GeneratorProperties {

    private int rate = 1;
    private int customers = 1;
    private int subscriptionPause = 3;
    private int resubscriptionInterval = 1;
    private int pendingTime = 10;
    private int onrouteTime = 30;
    private int ttl = 60;

}
