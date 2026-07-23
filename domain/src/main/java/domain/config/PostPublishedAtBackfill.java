package domain.config;

import domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostPublishedAtBackfill {

    private final PostRepository postRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void backfill() {
        int updatedCount = postRepository.backfillPublishedAt();
        if (updatedCount > 0) {
            log.info("[마이그레이션] 기존 PUBLISHED 게시글 publishedAt 보정: {}건", updatedCount);
        }
    }
}
