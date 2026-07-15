package application.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

public class DiscordNotifier {

    private static final Logger log = LoggerFactory.getLogger(DiscordNotifier.class);
    private static final int DISCORD_CONTENT_LIMIT = 2_000;

    private final boolean enabled;
    private final String webhookUrl;
    private final RestClient restClient;

    public DiscordNotifier(boolean enabled, String webhookUrl, long timeoutMillis) {
        this.enabled = enabled;
        this.webhookUrl = webhookUrl;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofMillis(Math.max(1, timeoutMillis));
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Async("discordNotificationExecutor")
    public void send(String title, String message) {
        if (!enabled || !StringUtils.hasText(webhookUrl)) {
            return;
        }

        String content = truncate(title + "\n" + message);
        try {
            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("content", content))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Discord 알림 전송 실패. title={}", title, e);
        }
    }

    private String truncate(String content) {
        if (content.length() <= DISCORD_CONTENT_LIMIT) {
            return content;
        }
        return content.substring(0, DISCORD_CONTENT_LIMIT - 3) + "...";
    }
}
