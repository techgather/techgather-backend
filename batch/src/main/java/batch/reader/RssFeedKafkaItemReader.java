package batch.reader;

import batch.message.RssFeedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.kafka.KafkaItemReader;
import org.springframework.batch.item.kafka.builder.KafkaItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Properties;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RssFeedKafkaItemReader {

    private final Properties kafkaConsumerProperties;

    @Value("${spring.kafka.consumer.topic}")
    private String topic;

    @Value("${spring.kafka.consumer.poll-timeout:5000}")
    private long pollTimeout;

    @Bean
    @StepScope
    public KafkaItemReader<String, RssFeedMessage> kafkaItemReader() {
        return new KafkaItemReaderBuilder<String, RssFeedMessage>()
                .name("rss-kafka-item-reader")
                .topic(topic)
                .partitions(0)  // 현재 단일 파티션 지원. 추후 확장 시 다중 파티션 지원 필요
                .consumerProperties(kafkaConsumerProperties)
                .pollTimeout(Duration.ofMillis(pollTimeout))
                .saveState(true)
                .build();
    }

}