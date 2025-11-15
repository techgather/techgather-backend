package batch.config;

import batch.message.RssFeedMessage;
import batch.writer.RssFeedWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.kafka.KafkaItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import static batch.constants.BatchConstants.*;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final KafkaItemReader<String, RssFeedMessage> kafkaItemReader;
    private final RssFeedWriter rssItemWriter;

    @Bean(RSS_COLLECT_JOB_NAME + "_job")
    public Job rssFeedsCollectJob() {
        return new JobBuilder(RSS_COLLECT_JOB_NAME, jobRepository)
                .start(rssFeedsCollectStep())
                .build();
    }

    @Bean(RSS_COLLECT_JOB_NAME + "_step")
    public Step rssFeedsCollectStep() {
        return new StepBuilder(RSS_COLLECT_JOB_NAME + "_step", jobRepository)
                .<RssFeedMessage, RssFeedMessage>chunk(CHUNK_SIZE, transactionManager)
                .reader(kafkaItemReader)
                .writer(rssItemWriter)
                .stream(kafkaItemReader)
                .build();
    }
}
