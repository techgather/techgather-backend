package batch.reader;

import batch.message.RssFeedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.kafka.KafkaItemReader;
import org.springframework.batch.item.kafka.builder.KafkaItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;
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
        TopicPartition partition = new TopicPartition(topic, 0);
        long startOffset = resolveStartOffset(partition);
        Map<TopicPartition, Long> partitionOffsets = new HashMap<>();
        partitionOffsets.put(partition, startOffset);

        log.info("Kafka reader start offset resolved for {}-{}: {}", partition.topic(), partition.partition(), startOffset);

        return new KafkaItemReaderBuilder<String, RssFeedMessage>()
                .name("rss-kafka-item-reader")
                .topic(topic)
                .partitions(0)  // 현재 단일 파티션 지원. 추후 확장 시 다중 파티션 지원 필요
                .partitionOffsets(partitionOffsets)
                .consumerProperties(kafkaConsumerProperties)
                .pollTimeout(Duration.ofMillis(pollTimeout))
                .saveState(false)
                .build();
    }

    private long resolveStartOffset(TopicPartition partition) {
        try (KafkaConsumer<String, RssFeedMessage> consumer = new KafkaConsumer<>(kafkaConsumerProperties)) {
            consumer.assign(Collections.singletonList(partition));

            OffsetAndMetadata committed = consumer.committed(partition);
            if (committed != null) {
                return committed.offset();
            }

            consumer.seekToBeginning(Collections.singletonList(partition));
            return consumer.position(partition);
        } catch (Exception e) {
            log.warn("Failed to resolve start offset for {}-{}, fallback to 0", partition.topic(), partition.partition(), e);
            return 0L;
        }
    }
}
