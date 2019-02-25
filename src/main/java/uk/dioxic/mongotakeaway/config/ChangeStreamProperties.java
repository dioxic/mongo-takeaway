package uk.dioxic.mongotakeaway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("classpath:takeaway.properties")
@ConfigurationProperties(prefix = "changestream")
public class ChangeStreamProperties {

    private int subscriptionPause = 3;
    private int resubscriptionInterval = 1;

}
