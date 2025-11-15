package application.generator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.random.RandomGenerator;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SnowFlake {

	private static final int UNUSED_BITS = 1;
	private static final int EPOCH_BITS = 41;
	private static final int NODE_ID_BITS = 10;
	private static final int SEQUENCE_BITS = 12;

	private static final long maxNodeId = (1L << NODE_ID_BITS) - 1;
	private static final long maxSequence = (1L << SEQUENCE_BITS) - 1;

	private static final SnowFlake INSTANCE = new SnowFlake();

	private final long nodeId = RandomGenerator.getDefault().nextLong(maxNodeId + 1);
	private final long startTimeMillis = 1704067200000L;

	private long lastTimeMillis = startTimeMillis - 1;
	private long sequence = 0L;

	public static SnowFlake getInstance() {
		return INSTANCE;
	}

	public synchronized long nextId() {
		long currentTimeMillis = System.currentTimeMillis();

		if (currentTimeMillis < lastTimeMillis) {
			throw new IllegalStateException("Invalid Time");
		}

		if (currentTimeMillis != lastTimeMillis) {
			lastTimeMillis = currentTimeMillis;
			sequence = 0;
		}

		sequence = (sequence + 1) & maxSequence;
		if (sequence == 0) {
			currentTimeMillis = waitNextMillis(currentTimeMillis);
			lastTimeMillis = currentTimeMillis;
			sequence = 0;
		}


		return ((currentTimeMillis - startTimeMillis) << (NODE_ID_BITS + SEQUENCE_BITS))
				| (nodeId << SEQUENCE_BITS)
				| sequence;
	}

	private long waitNextMillis(long currentTimestamp) {
		while (currentTimestamp <= lastTimeMillis) {
			currentTimestamp = System.currentTimeMillis();
		}
		return currentTimestamp;
	}
}