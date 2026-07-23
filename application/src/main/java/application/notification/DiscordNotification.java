package application.notification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record DiscordNotification(
        Severity severity,
        String title,
        String description,
        List<Field> fields,
        String footer,
        Instant timestamp
) {

    public DiscordNotification {
        severity = severity == null ? Severity.INFO : severity;
        title = title == null ? "" : title;
        description = description == null ? "" : description;
        fields = fields == null ? List.of() : List.copyOf(fields);
        footer = footer == null ? "TechGather" : footer;
        timestamp = timestamp == null ? Instant.now() : timestamp;
    }

    public static Builder builder(Severity severity, String title) {
        return new Builder(severity, title);
    }

    public enum Severity {
        SUCCESS(0x2ECC71, "✅"),
        INFO(0x3498DB, "ℹ️"),
        WARNING(0xF1C40F, "⚠️"),
        ERROR(0xE74C3C, "❌");

        private final int color;
        private final String emoji;

        Severity(int color, String emoji) {
            this.color = color;
            this.emoji = emoji;
        }

        public int getColor() {
            return color;
        }

        public String getEmoji() {
            return emoji;
        }
    }

    public record Field(String name, String value, boolean inline) {
        public Field {
            name = name == null ? "" : name;
            value = value == null ? "" : value;
        }
    }

    public static final class Builder {
        private final Severity severity;
        private final String title;
        private final List<Field> fields = new ArrayList<>();
        private String description = "";
        private String footer = "TechGather";
        private Instant timestamp = Instant.now();

        private Builder(Severity severity, String title) {
            this.severity = severity;
            this.title = title;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder field(String name, String value) {
            return field(name, value, false);
        }

        public Builder field(String name, String value, boolean inline) {
            fields.add(new Field(name, value, inline));
            return this;
        }

        public Builder footer(String footer) {
            this.footer = footer;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public DiscordNotification build() {
            return new DiscordNotification(severity, title, description, fields, footer, timestamp);
        }
    }
}
