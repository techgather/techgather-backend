package application.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DiscordNotifier {

    private static final Logger log = LoggerFactory.getLogger(DiscordNotifier.class);
    private static final int EMBED_TOTAL_LIMIT = 6_000;
    private static final int EMBED_TITLE_LIMIT = 256;
    private static final int EMBED_DESCRIPTION_LIMIT = 4_096;
    private static final int EMBED_FIELD_COUNT_LIMIT = 25;
    private static final int EMBED_FIELD_NAME_LIMIT = 256;
    private static final int EMBED_FIELD_VALUE_LIMIT = 1_024;
    private static final int EMBED_FOOTER_LIMIT = 2_048;

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
    public void send(DiscordNotification notification) {
        if (!enabled || !StringUtils.hasText(webhookUrl)) {
            return;
        }

        try {
            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(createPayload(notification))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Discord 알림 전송 실패. title={}", notification.title(), e);
        }
    }

    Map<String, Object> createPayload(DiscordNotification notification) {
        Map<String, Object> embed = new LinkedHashMap<>();
        int remaining = EMBED_TOTAL_LIMIT;

        String title = notification.severity().getEmoji() + " " + notification.title();
        title = truncate(title, Math.min(EMBED_TITLE_LIMIT, remaining));
        embed.put("title", title);
        remaining -= title.length();

        if (StringUtils.hasText(notification.description()) && remaining > 0) {
            String description = truncate(
                    notification.description(),
                    Math.min(EMBED_DESCRIPTION_LIMIT, remaining)
            );
            embed.put("description", description);
            remaining -= description.length();
        }

        List<Map<String, Object>> fields = new ArrayList<>();
        for (DiscordNotification.Field field : notification.fields()) {
            if (fields.size() == EMBED_FIELD_COUNT_LIMIT || remaining <= 2) {
                break;
            }

            String name = truncate(field.name(), Math.min(EMBED_FIELD_NAME_LIMIT, remaining - 1));
            remaining -= name.length();
            String value = truncate(
                    StringUtils.hasText(field.value()) ? field.value() : "-",
                    Math.min(EMBED_FIELD_VALUE_LIMIT, remaining)
            );
            remaining -= value.length();

            fields.add(Map.of(
                    "name", name,
                    "value", value,
                    "inline", field.inline()
            ));
        }
        if (!fields.isEmpty()) {
            embed.put("fields", fields);
        }

        if (StringUtils.hasText(notification.footer()) && remaining > 0) {
            String footer = truncate(notification.footer(), Math.min(EMBED_FOOTER_LIMIT, remaining));
            embed.put("footer", Map.of("text", footer));
        }
        embed.put("color", notification.severity().getColor());
        embed.put("timestamp", notification.timestamp().toString());

        return Map.of(
                "embeds", List.of(embed),
                "allowed_mentions", Map.of("parse", List.of())
        );
    }

    private String truncate(String content, int limit) {
        if (limit <= 0) {
            return "";
        }
        if (content.length() <= limit) {
            return content;
        }
        if (limit <= 3) {
            return content.substring(0, limit);
        }
        return content.substring(0, limit - 3) + "...";
    }
}
