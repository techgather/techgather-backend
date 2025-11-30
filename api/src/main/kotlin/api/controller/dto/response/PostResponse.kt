package api.controller.dto.response

import api.service.dto.result.PostResult
import java.time.LocalDateTime

data class PostResponse(
    val postId: Long,
    val title: String,
    val pubDate: LocalDateTime,
    val thumbnail: String,
    val url: String,
    val tags: List<String>
) {

    companion object {
        fun from(result: PostResult): PostResponse {
            return PostResponse(
                postId = result.postId,
                title = result.title,
                pubDate = result.pubDate,
                thumbnail = result.thumbnail,
                url = result.url,
                tags = result.tags
            )
        }
    }
}
