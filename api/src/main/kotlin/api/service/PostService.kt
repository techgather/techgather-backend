package api.service

import api.controller.dto.request.PostSearchCondition
import api.service.dto.result.PostResults
import domain.constants.Language
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
        language: Language?,
        lastPostId: Long?,
        limit: Long
    ): PostResults {

        val posts = when (lastPostId) {
            null -> postRepository.searchPosts(language, postSearchCondition.keyword, limit + 1)
            else -> postRepository.searchPosts(language, postSearchCondition.keyword, lastPostId, limit + 1)
        }
            .toList()
            .onEach { it.postTags.size }

        return PostResults.of(posts, limit)
    }
}

