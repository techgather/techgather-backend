package application.notification;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordNotifierTest {

    private final DiscordNotifier notifier = new DiscordNotifier(false, "", 5_000);

    @Test
    @SuppressWarnings("unchecked")
    void createsStructuredEmbedPayload() {
        Instant timestamp = Instant.parse("2026-07-23T03:00:00Z");
        DiscordNotification notification = DiscordNotification
                .builder(DiscordNotification.Severity.WARNING, "게시글 수집 부분 완료")
                .description("수집 및 적재 결과")
                .field("수집 성공", "30 / 31개", true)
                .field("실패 사이트", "• toss — timeout")
                .footer("TechGather · collector")
                .timestamp(timestamp)
                .build();

        Map<String, Object> payload = notifier.createPayload(notification);

        List<Map<String, Object>> embeds = (List<Map<String, Object>>) payload.get("embeds");
        Map<String, Object> embed = embeds.get(0);
        List<Map<String, Object>> fields = (List<Map<String, Object>>) embed.get("fields");

        assertThat(embed.get("title")).isEqualTo("⚠️ 게시글 수집 부분 완료");
        assertThat(embed.get("description")).isEqualTo("수집 및 적재 결과");
        assertThat(embed.get("color")).isEqualTo(0xF1C40F);
        assertThat(embed.get("timestamp")).isEqualTo(timestamp.toString());
        assertThat(embed.get("footer")).isEqualTo(Map.of("text", "TechGather · collector"));
        assertThat(fields)
                .containsExactly(
                        Map.of("name", "수집 성공", "value", "30 / 31개", "inline", true),
                        Map.of("name", "실패 사이트", "value", "• toss — timeout", "inline", false)
                );
        assertThat(payload.get("allowed_mentions")).isEqualTo(Map.of("parse", List.of()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void respectsDiscordEmbedLimits() {
        DiscordNotification.Builder builder = DiscordNotification
                .builder(DiscordNotification.Severity.ERROR, "제목".repeat(200))
                .description("설명".repeat(3_000));
        for (int index = 0; index < 30; index++) {
            builder.field("필드".repeat(200), "값".repeat(2_000));
        }

        Map<String, Object> payload = notifier.createPayload(builder.build());
        Map<String, Object> embed = ((List<Map<String, Object>>) payload.get("embeds")).get(0);
        List<Map<String, Object>> fields =
                (List<Map<String, Object>>) embed.getOrDefault("fields", List.of());

        assertThat(((String) embed.get("title")).length()).isLessThanOrEqualTo(256);
        assertThat(((String) embed.get("description")).length()).isLessThanOrEqualTo(4_096);
        assertThat(fields).hasSizeLessThanOrEqualTo(25);
        assertThat(fields).allSatisfy(field -> {
            assertThat(((String) field.get("name")).length()).isLessThanOrEqualTo(256);
            assertThat(((String) field.get("value")).length()).isLessThanOrEqualTo(1_024);
        });
    }
}
