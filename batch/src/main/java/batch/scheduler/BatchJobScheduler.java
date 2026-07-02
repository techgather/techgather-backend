package batch.scheduler;

import batch.service.PostReleaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchJobScheduler {

    private final PostReleaseService postReleaseService;

    public BatchJobScheduler(PostReleaseService postReleaseService) {
        this.postReleaseService = postReleaseService;
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
