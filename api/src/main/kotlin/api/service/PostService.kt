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
import java.time.LocalDateTime

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
        lastPostId: String?,
        limit: Long
    ): PostResults {
        val requestedStatus = status ?: PostStatus.NOT_PUBLISHED
        val parsedLastPostId = parseId(lastPostId, "lastPostId")
        val parsedCategoryIds = parseIds(postSearchCondition.categoryIds, "categoryIds")
        val cursorPubDate = resolveCursorPubDate(parsedLastPostId)

        val posts = when (parsedLastPostId) {
            null -> postRepository.searchPosts(
                language,
                postSearchCondition.keyword,
                parsedCategoryIds,
                postSearchCondition.sourceSiteName,
                requestedStatus,
                limit + 1
            )
            else -> postRepository.searchPosts(
                language,
                postSearchCondition.keyword,
                parsedCategoryIds,
                postSearchCondition.sourceSiteName,
                requestedStatus,
                cursorPubDate,
                parsedLastPostId,
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
        postIds: List<String>,
        status: PostStatus,
        categoryIds: List<String>? = null
    ) {
        val parsedPostIds = parseIds(postIds, "postIds")
        val parsedCategoryIds = parseIds(categoryIds, "categoryIds")

        val existingPostIds = postRepository.findPostByPostIdIn(parsedPostIds)
        postRepository.updateStatusByPostId(existingPostIds, status)

        if (parsedCategoryIds == null || existingPostIds.isEmpty()) {
            return
        }

        postCategoryRepository.deleteAllByPostPostIdIn(existingPostIds)

        val distinctCategoryIds = parsedCategoryIds.distinct()
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

    @Transactional(readOnly = true)
    fun getSourceSiteNamesForUser(): List<String> {
        return postRepository.findDistinctSourceSiteNames(PostStatus.PUBLISHED)
    }

    @Transactional(readOnly = true)
    fun getSourceSiteNamesForAdmin(status: PostStatus?): List<String> {
        val requestedStatus = status ?: PostStatus.NOT_PUBLISHED
        return postRepository.findDistinctSourceSiteNames(requestedStatus)
    }

    private fun parseIds(ids: List<String>?, fieldName: String): List<Long>? {
        if (ids == null) {
            return null
        }
        return ids.map { parseId(it, fieldName) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$fieldName contains null value") }
    }

    private fun parseId(id: String?, fieldName: String): Long? {
        if (id == null) {
            return null
        }
        return id.toLongOrNull()
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid $fieldName: $id")
    }

    private fun resolveCursorPubDate(lastPostId: Long?): LocalDateTime? {
        if (lastPostId == null) {
            return null
        }

        return postRepository.findById(lastPostId)
            .map { it.pubDate }
            .orElseThrow {
                ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid lastPostId: $lastPostId")
            }
    }
}
