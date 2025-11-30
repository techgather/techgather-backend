package api.controller.dto.response

import api.service.dto.result.PostResults

data class PostResponseList(
    val posts: List<PostResponse>,
    val hasNext: Boolean,
    val nextPostId: Long?
) {

    companion object {
        val EMPTY = PostResponseList(
            posts = emptyList(),
            hasNext = false,
            nextPostId = null
        )

        fun from(results: PostResults): PostResponseList {
            if (results.postResults.isEmpty()) {
                return EMPTY
            }

            return PostResponseList(
                posts = results.postResults.map { PostResponse.from(it) },
                hasNext = results.hasNext,
                nextPostId = results.nextPostId
            )
        }
    }
}
