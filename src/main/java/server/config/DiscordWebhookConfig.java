package server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordWebhookConfig {

    @Value("${discord.webhook.url:}")
    private String webhookUrl;

    public String getWebhookUrl() {
        return webhookUrl;
    }
} 
