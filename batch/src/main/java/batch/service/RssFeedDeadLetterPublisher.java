package batch.service;

import batch.message.RssFeedDeadLetterMessage;
import batch.message.RssFeedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RssFeedDeadLetterPublisher {

	private final KafkaTemplate<String, RssFeedDeadLetterMessage> kafkaTemplate;
	private final String topic;
	private final long publishTimeoutMillis;

	public RssFeedDeadLetterPublisher(
			@Qualifier("dltKafkaTemplate") KafkaTemplate<String, RssFeedDeadLetterMessage> kafkaTemplate,
			@Value("${spring.kafka.dlt.topic:post.DLT}") String topic,
			@Value("${spring.kafka.dlt.publish-timeout-millis:10000}") long publishTimeoutMillis
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.topic = topic;
		this.publishTimeoutMillis = publishTimeoutMillis;
	}

	public void publish(RssFeedMessage originalMessage, Throwable failure, int retryCount) {
		RssFeedDeadLetterMessage deadLetterMessage = new RssFeedDeadLetterMessage(
				originalMessage,
				failure.getClass().getName(),
				failure.getMessage() == null ? failure.toString() : failure.getMessage(),
				retryCount,
				LocalDateTime.now()
		);

		try {
			kafkaTemplate.send(topic, originalMessage.url(), deadLetterMessage)
					.get(publishTimeoutMillis, TimeUnit.MILLISECONDS);
			log.error(
					"Message moved to Kafka DLT. topic={}, url={}, retryCount={}",
					topic,
					originalMessage.url(),
					retryCount
			);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while publishing message to Kafka DLT", e);
		} catch (Exception e) {
			throw new IllegalStateException(
					"Failed to publish message to Kafka DLT. topic=" + topic,
					e
			);
		}
	}
}
