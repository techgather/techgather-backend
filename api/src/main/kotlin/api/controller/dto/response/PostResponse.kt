package api.controller.dto.response

import api.service.dto.result.PostResult
import domain.constants.Language
import java.time.LocalDateTime

data class PostResponse(
    val postId: Long,
    val title: String,
    val pubDate: LocalDateTime,
    val thumbnail: String,
    val url: String,
    val tags: List<String>,
    val categories: List<PostCategoryResponse>,
    val sourceSiteName: String,
    val language: Language
) {

    companion object {
        fun from(result: PostResult): PostResponse {
            return PostResponse(
                postId = result.postId,
                title = result.title,
                pubDate = result.pubDate,
                thumbnail = result.thumbnail,
                url = result.url,
                tags = result.tags,
                categories = result.categories.map { PostCategoryResponse.from(it.groupName, it.categoryName) },
                sourceSiteName = result.sourceSiteName,
                language = result.language
            )
        }
    }
}

data class PostCategoryResponse(
    val groupName: String,
    val categoryName: String
) {
    companion object {
        fun from(groupName: String, categoryName: String): PostCategoryResponse {
            return PostCategoryResponse(
                groupName = groupName,
                categoryName = categoryName
            )
        }
    }
}
