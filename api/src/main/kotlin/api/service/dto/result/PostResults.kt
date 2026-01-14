package api.service.dto.result

import domain.entity.Post

data class PostResults(
    val postResults: List<PostResult>,
    val hasNext: Boolean,
    val nextPostId: Long?
) {
    companion object {
        fun of(posts: List<Post>, limit: Long): PostResults {
            val hasNext = posts.size > limit
            val resultPosts = if (hasNext) posts.dropLast(1) else posts
            val nextPostId = if (hasNext) posts.last().postId else null
            return PostResults(
                postResults = resultPosts.map { PostResult.from(it) },
                hasNext = hasNext,
                nextPostId = nextPostId
            )
        }
    }
}