package batch.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BatchConstants {

	public static final String RSS_COLLECT_JOB_NAME = "RSS_FEED_COLLECT";
	public static final int CHUNK_SIZE = 50;
}
