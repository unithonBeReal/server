package server.config;

import io.ably.lib.rest.AblyRest;
import io.ably.lib.types.AblyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AblyConfig {

    @Value("${ably.api-key}")
    private String apiKey;

    @Bean
    public AblyRest ablyRest() throws AblyException {
        return new AblyRest(apiKey);
    }
} 
