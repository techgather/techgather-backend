package api.service.dto.result

import domain.constants.Language
import domain.entity.Post
import java.time.LocalDateTime

data class PostResult(
    val postId: Long,
    val title: String,
    val pubDate: LocalDateTime,
    val publishedAt: LocalDateTime?,
    val thumbnail: String,
    val url: String,
    val tags: List<String>,
    val categories: List<PostCategoryResult>,
    val sourceSiteName: String,
    val language: Language
) {

    companion object {
        fun from(post: Post): PostResult {
            return PostResult(
                postId = post.postId,
                title = post.title,
                pubDate = post.pubDate,
                publishedAt = post.publishedAt,
                thumbnail = post.thumbnail,
                url = post.url,
                tags = post.postTags.map { it.tag.name },
                categories = post.postCategories.map {
                    PostCategoryResult(
                        groupName = it.category.categoryGroup.name,
                        categoryName = it.category.name
                    )
                },
                sourceSiteName = post.sourceSiteName,
                language = post.language
            )
        }
    }
}

data class PostCategoryResult(
    val groupName: String,
    val categoryName: String
)
