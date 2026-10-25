package batch.scheduler;

import batch.service.PostIngestClassificationService;
import batch.service.PostReleaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchJobScheduler {

    private final PostReleaseService postReleaseService;
    private final PostIngestClassificationService postIngestClassificationService;

    public BatchJobScheduler(PostReleaseService postReleaseService,
                             PostIngestClassificationService postIngestClassificationService) {
        this.postReleaseService = postReleaseService;
        this.postIngestClassificationService = postIngestClassificationService;
    }

    @Scheduled(cron = "${classification.schedule.cron:0 */10 * * * *}", zone = "${classification.schedule.zone:Asia/Seoul}")
    public void classifyUnclassifiedPosts() {
        try {
            postIngestClassificationService.classifyUnclassifiedPosts();
        } catch (Exception e) {
            log.error("[스케줄] 미분류 게시글 분류 실패", e);
        }
    }

    @Scheduled(cron = "${batch.schedule.release-cron:0 0 0 * * *}", zone = "${batch.schedule.zone:Asia/Seoul}")
    public void releaseReservedPosts() {
        log.info("[스케줄] 예약 게시글 공개 시작");
        try {
            postReleaseService.releaseReservedPosts();
            log.info("[스케줄] 예약 게시글 공개 종료");
        } catch (Exception e) {
            log.error("[스케줄] 예약 게시글 공개 실패", e);
        }
    }
}
