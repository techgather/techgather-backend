package application.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class DiscordNotificationConfig {

    @Bean("discordNotificationExecutor")
    public Executor discordNotificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("discord-notification-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        return executor;
    }

    @Bean
    public DiscordNotifier discordNotifier(
            @Value("${discord.notification.enabled:false}") boolean enabled,
            @Value("${discord.notification.post-webhook-url:${discord.notification.webhook-url:}}") String webhookUrl,
            @Value("${discord.notification.timeout-millis:5000}") long timeoutMillis
    ) {
        return new DiscordNotifier(enabled, webhookUrl, timeoutMillis);
    }

    @Bean("feedbackDiscordNotifier")
    public DiscordNotifier feedbackDiscordNotifier(
            @Value("${discord.notification.enabled:false}") boolean enabled,
            @Value("${discord.notification.feedback-webhook-url:}") String webhookUrl,
            @Value("${discord.notification.timeout-millis:5000}") long timeoutMillis
    ) {
        return new DiscordNotifier(enabled, webhookUrl, timeoutMillis);
    }
}
