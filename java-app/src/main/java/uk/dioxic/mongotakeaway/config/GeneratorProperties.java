package uk.dioxic.mongotakeaway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("classpath:takeaway.properties")
@ConfigurationProperties(prefix = "generator")
public class GeneratorProperties {

    private int rate = 1;
    private int concurrency = 1;
    private boolean randomise = true;
    private int customers = 1;
    private int menuItems = 100;
    private int jobInterval = 10;
    private int pendingTime = 10;
    private int onrouteTime = 30;
    private int ttl = 60;
    private int batchSize = 1000;
    private boolean dropCollection;

    /**
     * Disable batching unless rate is higher than threshold
     * @return the adjusted batch size
     */
    public int adjustedBatchSize() {
        return true ? batchSize : 1;
    }

    public boolean isBatching() {
        return rate < 1 || rate >= 1000 && batchSize > 0;
    }

}
