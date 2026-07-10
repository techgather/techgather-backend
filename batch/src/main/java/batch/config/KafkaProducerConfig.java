package batch.config;

import batch.message.RssFeedDeadLetterMessage;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class KafkaProducerConfig {

	@Bean("dltKafkaTemplate")
	public KafkaTemplate<String, RssFeedDeadLetterMessage> dltKafkaTemplate(
			@Qualifier("kafkaConsumerProperties") Properties consumerProperties
	) {
		Map<String, Object> producerProperties = new HashMap<>();
		producerProperties.put(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
				consumerProperties.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)
		);
		producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		producerProperties.put(ProducerConfig.ACKS_CONFIG, "all");

		String securityProtocol = consumerProperties.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG);
		if (StringUtils.hasText(securityProtocol)) {
			producerProperties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
			producerProperties.put(
					SaslConfigs.SASL_MECHANISM,
					consumerProperties.getProperty(SaslConfigs.SASL_MECHANISM)
			);
			producerProperties.put(
					SaslConfigs.SASL_JAAS_CONFIG,
					consumerProperties.getProperty(SaslConfigs.SASL_JAAS_CONFIG)
			);
		}

		return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProperties));
	}
}
