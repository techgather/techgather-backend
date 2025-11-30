
package batch.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static batch.constants.BatchConstants.RSS_COLLECT_JOB_NAME;

@Slf4j
@Component
public class BatchJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job rssFeedsCollectJob;

    public BatchJobScheduler(JobLauncher jobLauncher,
                             @Qualifier(RSS_COLLECT_JOB_NAME + "_job") Job rssFeedsCollectJob) {
        this.jobLauncher = jobLauncher;
        this.rssFeedsCollectJob = rssFeedsCollectJob;
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void runRssFeedsCollectJob() {
        try {
            String createDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("createDate", createDate)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(rssFeedsCollectJob, jobParameters);
        } catch (Exception e) {
            log.error("Failed to run RSS Feeds Collect Job", e);
        }
    }
}