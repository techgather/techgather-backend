package api.service

import api.controller.dto.request.PostSearchCondition
import api.service.dto.result.PostResults
import application.generator.SnowFlake
import domain.constants.Language
import domain.constants.PostStatus
import domain.entity.PostCategory
import domain.repository.CategoryRepository
import domain.repository.PostCategoryRepository
import domain.repository.PostRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

@Service
class PostService(
    private val postRepository: PostRepository,
    private val categoryRepository: CategoryRepository,
    private val postCategoryRepository: PostCategoryRepository
) {

    private val snowFlake = SnowFlake.getInstance()

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
        status: PostStatus,
        categoryIds: List<Long>? = null
    ) {
        val existingPostIds = postRepository.findPostByPostIdIn(postIds)
        postRepository.updateStatusByPostId(existingPostIds, status)

        if (categoryIds == null || existingPostIds.isEmpty()) {
            return
        }

        postCategoryRepository.deleteAllByPostPostIdIn(existingPostIds)

        val distinctCategoryIds = categoryIds.distinct()
        if (distinctCategoryIds.isEmpty()) {
            return
        }

        val categories = categoryRepository.findAllById(distinctCategoryIds)
        if (categories.size != distinctCategoryIds.size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리 ID가 포함되어 있습니다.")
        }

        val posts = postRepository.findAllById(existingPostIds)
        val newMappings = mutableListOf<PostCategory>()
        for (post in posts) {
            for (category in categories) {
                newMappings.add(PostCategory.create(snowFlake.nextId(), post, category))
            }
        }

        postCategoryRepository.saveAll(newMappings)
    }

}
