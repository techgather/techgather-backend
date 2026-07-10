package batch.message;

import java.time.LocalDateTime;

public record RssFeedDeadLetterMessage(
		RssFeedMessage originalMessage,
		String failureType,
		String failureMessage,
		int retryCount,
		LocalDateTime failedAt
) {
}
