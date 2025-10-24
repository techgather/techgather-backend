package collector.adapter.publisher.kafka

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("spring.kafka")
data class KafkaProperty (
    val topic: String
)