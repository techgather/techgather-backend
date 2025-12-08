package api.service.dto.result

import domain.entity.Post

data class PostResults(
    val postResults: List<PostResult>,
    val hasNext: Boolean,
    val nextPostId: Long?
) {
    companion object {
        fun from(posts: List<Post>): PostResults {
            return PostResults(
                postResults = posts.map { PostResult.from(it) },
                hasNext = false,
                nextPostId = null
            )
        }

        fun of(posts: List<Post>, hasNext: Boolean, nextPostId: Long?): PostResults {
            return PostResults(
                postResults = posts.map { PostResult.from(it) },
                hasNext = hasNext,
                nextPostId = nextPostId
            )
        }
    }
}