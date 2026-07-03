package batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static batch.constants.BatchConstants.RSS_COLLECT_JOB_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostIngestJobService {

    private final JobLauncher jobLauncher;
    @Qualifier(RSS_COLLECT_JOB_NAME + "_job")
    private final Job rssFeedsCollectJob;
    private final PostClassifyService postClassifyService;

    public PostIngestResult runPostIngestAndClassify() throws Exception {
        log.info("[스케줄] Kafka 게시글 적재 job 시작");

        String createDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("createDate", createDate)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(rssFeedsCollectJob, jobParameters);
        BatchStatus jobStatus = jobExecution.getStatus();
        PostIngestResult result = toResult(jobExecution, false);

        if (jobStatus != BatchStatus.COMPLETED) {
            log.warn("[스케줄] Kafka 게시글 적재 job 비정상 종료. result={}", result);
            return result;
        }

        log.info("[스케줄] Kafka 게시글 적재 job 종료");
        log.info("[스케줄] 게시글 분류 시작");
        postClassifyService.classifyUnclassifiedPosts();
        log.info("[스케줄] 게시글 분류 종료");

        return toResult(jobExecution, true);
    }

    private PostIngestResult toResult(JobExecution jobExecution, boolean classified) {
        List<StepResult> steps = jobExecution.getStepExecutions().stream()
                .map(step -> new StepResult(
                        step.getStepName(),
                        step.getStatus().name(),
                        step.getExitStatus().getExitCode(),
                        step.getReadCount(),
                        step.getWriteCount(),
                        step.getCommitCount(),
                        step.getRollbackCount()
                ))
                .toList();

        List<String> failureMessages = jobExecution.getAllFailureExceptions().stream()
                .map(Throwable::toString)
                .toList();

        return new PostIngestResult(
                jobExecution.getJobId(),
                jobExecution.getId(),
                jobExecution.getStatus().name(),
                jobExecution.getExitStatus().getExitCode(),
                jobExecution.getExitStatus().getExitDescription(),
                classified,
                steps,
                failureMessages
        );
    }

    public record PostIngestResult(
            Long jobId,
            Long jobExecutionId,
            String jobStatus,
            String exitCode,
            String exitDescription,
            boolean classified,
            List<StepResult> steps,
            List<String> failureMessages
    ) {
    }

    public record StepResult(
            String stepName,
            String status,
            String exitCode,
            long readCount,
            long writeCount,
            long commitCount,
            long rollbackCount
    ) {
    }
}
