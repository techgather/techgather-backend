package api.service

import api.controller.dto.request.PostSearchCondition
import api.service.dto.result.PostResults
import domain.constants.Language
import domain.constants.PostStatus
import domain.repository.PostRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostService(
    private val postRepository: PostRepository
) {

    @Transactional(readOnly = true)
    fun getPosts(
        postSearchCondition: PostSearchCondition,
        status: PostStatus?,
        language: Language?,
        lastPostId: Long?,
        limit: Long
    ): PostResults {
        val requestedStatus = status ?: PostStatus.NOT_PUBLISHED

        val posts = when (lastPostId) {
            null -> postRepository.searchPosts(
                language,
                postSearchCondition.keyword,
                postSearchCondition.categoryIds,
                requestedStatus,
                limit + 1
            )
            else -> postRepository.searchPosts(
                language,
                postSearchCondition.keyword,
                postSearchCondition.categoryIds,
                requestedStatus,
                lastPostId,
                limit + 1
            )
        }
            .toList()
            .onEach {
                it.postTags.size
                it.postCategories.size
            }

        return PostResults.of(posts, limit)
    }

    @Transactional
    fun markedPostStatus(
        postIds: List<Long>,
        status: PostStatus
    ) {
        val postIds = postRepository.findPostByPostIdIn(postIds)
        postRepository.updateStatusByPostId(postIds, status)
    }

}
