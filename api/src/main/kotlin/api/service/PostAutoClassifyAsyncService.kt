package api.service

import api.service.dto.result.ClassifyPostsResult
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class PostAutoClassifyAsyncService(
    private val postAutoClassifyService: PostAutoClassifyService
) {

    private val log = LoggerFactory.getLogger(PostAutoClassifyAsyncService::class.java)

    @Async("postClassificationTaskExecutor")
    fun classifyPosts(postIds: List<String>) {
        try {
            val result = postAutoClassifyService.classifyPosts(postIds)
            log.info(
                "[분류] 비동기 게시글 분류 완료. requested={}, found={}, classified={}, onHold={}, missing={}",
                result.requested,
                result.found,
                result.classifiedPostIds.size,
                result.onHoldPostIds.size,
                result.missingPostIds.size
            )
        } catch (e: Exception) {
            log.error("[분류] 비동기 게시글 분류 실패. postIds={}", postIds, e)
        }
    }
}
