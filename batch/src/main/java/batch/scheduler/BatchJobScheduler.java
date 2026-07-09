package batch.scheduler;

import batch.service.PostClassifyService;
import batch.service.PostReleaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchJobScheduler {

    private final PostReleaseService postReleaseService;
    private final PostClassifyService postClassifyService;

    @Value("${classification.schedule.enabled:true}")
    private boolean classificationScheduleEnabled;

    public BatchJobScheduler(PostReleaseService postReleaseService, PostClassifyService postClassifyService) {
        this.postReleaseService = postReleaseService;
        this.postClassifyService = postClassifyService;
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

    @Scheduled(cron = "${classification.schedule.cron:0 * * * * *}", zone = "${classification.schedule.zone:Asia/Seoul}")
    public void classifyUnclassifiedPosts() {
        if (!classificationScheduleEnabled) {
            return;
        }

        log.info("[스케줄] 게시글 분류 시작");
        try {
            PostClassifyService.PostClassifyResult result = postClassifyService.classifyUnclassifiedPosts();
            log.info("[스케줄] 게시글 분류 종료. result={}", result);
        } catch (Exception e) {
            log.error("[스케줄] 게시글 분류 실패", e);
        }
    }
}
