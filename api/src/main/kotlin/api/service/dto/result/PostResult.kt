package api.service.dto.result

import domain.entity.Post
import java.time.LocalDateTime

data class PostResult(
    val postId: Long,
    val title: String,
    val pubDate: LocalDateTime,
    val thumbnail: String,
    val url: String,
    val tags: List<String>
) {

    companion object {
        fun from(post: Post): PostResult {
            return PostResult(
                postId = post.postId,
                title = post.title,
                pubDate = post.pubDate,
                thumbnail = post.thumbnail,
                url = post.url,
                tags = post.postTags.map { it.tag.name }
            )
        }
    }
}