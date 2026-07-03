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
import java.util.Locale

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
        val requestedLanguage = language ?: Language.KO
        val parsedLastPostId = parseId(lastPostId, "lastPostId")
        val normalizedCategorySlugs = parseCategorySlugs(postSearchCondition.categorySlugs)
        val normalizedSourceSiteNames = parseSourceSiteNames(postSearchCondition.sourceSiteNames)
        val keyword = postSearchCondition.keyword?.trim()?.takeIf { it.isNotEmpty() }
        val cursorPubDate = resolveCursorPubDate(parsedLastPostId)
        val totalCount = postRepository.countPosts(
            requestedLanguage,
            keyword,
            normalizedCategorySlugs,
            normalizedSourceSiteNames,
            requestedStatus,
            postSearchCondition.unclassified
        )

        val posts = when (parsedLastPostId) {
            null -> postRepository.searchPosts(
                requestedLanguage,
                keyword,
                normalizedCategorySlugs,
                normalizedSourceSiteNames,
                requestedStatus,
                postSearchCondition.unclassified,
                limit + 1
            )
            else -> postRepository.searchPosts(
                requestedLanguage,
                keyword,
                normalizedCategorySlugs,
                normalizedSourceSiteNames,
                requestedStatus,
                postSearchCondition.unclassified,
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

        return PostResults.of(posts, limit, totalCount)
    }

    @Transactional
    fun markedPostStatus(
        postIds: List<String>,
        status: PostStatus,
        categoryIds: List<String>? = null
    ) {
        val parsedPostIds = parseIds(postIds, "postIds")
        val existingPostIds = postRepository.findPostByPostIdIn(parsedPostIds)
        postRepository.updateStatusByPostId(existingPostIds, status)

        val parsedCategoryIds = parseIds(categoryIds, "categoryIds")
        if (parsedCategoryIds.isNullOrEmpty() || existingPostIds.isEmpty()) {
            return
        }

        postCategoryRepository.deleteAllByPostPostIdIn(existingPostIds)
        postCategoryRepository.flush()

        val categories = categoryRepository.findAllById(parsedCategoryIds)
        if (categories.size != parsedCategoryIds.size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리 ID가 포함되어 있습니다.")
        }

        val posts = postRepository.findAllById(existingPostIds)
        val newMappings = posts.flatMap { post ->
            categories.map { category -> PostCategory.create(snowFlake.nextId(), post, category) }
        }

        postCategoryRepository.saveAll(newMappings)
    }

    @Transactional(readOnly = true)
    fun getSourceSiteNamesForUser(language: Language?): List<String> {
        return postRepository.findDistinctSourceSiteNames(null, language ?: Language.KO)
    }

    @Transactional(readOnly = true)
    fun getSourceSiteNamesForAdmin(language: Language?): List<String> {
        return postRepository.findDistinctSourceSiteNames(null, language ?: Language.KO)
    }

    private fun parseIds(ids: List<String>?, fieldName: String): List<Long>? {
        if (ids == null) {
            return null
        }
        val parsedIds = ids
            .mapNotNull { parseId(it, fieldName) }
            .distinct()
        return parsedIds.ifEmpty { null }
    }

    private fun parseId(id: String?, fieldName: String): Long? {
        val normalizedId = id?.trim()
        if (normalizedId.isNullOrEmpty()) {
            return null
        }
        return normalizedId.toLongOrNull()
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid $fieldName: $normalizedId")
    }

    private fun parseSourceSiteNames(sourceSiteNames: List<String>?): List<String>? {
        if (sourceSiteNames == null) {
            return null
        }
        val normalized = sourceSiteNames
            .map { it.trim().lowercase(Locale.ROOT) }
            .filter { it.isNotEmpty() }
            .distinct()
        return normalized.ifEmpty { null }
    }

    private fun parseCategorySlugs(categorySlugs: List<String>?): List<String>? {
        if (categorySlugs == null) {
            return null
        }
        val normalized = categorySlugs
            .map { it.trim().lowercase(Locale.ROOT) }
            .filter { it.isNotEmpty() }
            .distinct()
        return normalized.ifEmpty { null }
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
