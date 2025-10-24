package collector.adapter.publisher.kafka

import collector.engine.port.Publisher
import collector.engine.model.Message
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Message>,
    private val property: KafkaProperty,
): Publisher {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun publish(messages: List<Message>) {
        messages.forEach { message ->
            try {
                kafkaTemplate.send(property.topic, message.url, message)
            } catch (e: Exception) {
                log.error("Failed to publish message to Kafka: ${message.title}", e)
            }
        }
    }
}