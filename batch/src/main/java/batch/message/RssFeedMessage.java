package batch.message;

import java.time.LocalDateTime;
import java.util.Set;

public record RssFeedMessage(String title,
							 String url,
							 LocalDateTime pubDate,
							 Set<String> tags,
							 String description,
							 String thumbnail) {

}