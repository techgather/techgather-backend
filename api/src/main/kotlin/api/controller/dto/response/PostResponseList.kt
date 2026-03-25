package api.controller.dto.response

import api.service.dto.result.PostResults
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer

data class PostResponseList(
    val posts: List<PostResponse>,
    val totalCount: Long,
    val hasNext: Boolean,
    @field:JsonSerialize(using = ToStringSerializer::class)
    val nextPostId: Long?
) {

    companion object {
        val EMPTY = PostResponseList(
            posts = emptyList(),
            totalCount = 0,
            hasNext = false,
            nextPostId = null
        )

        fun from(results: PostResults): PostResponseList {
            if (results.postResults.isEmpty()) {
                return EMPTY.copy(totalCount = results.totalCount)
            }

            return PostResponseList(
                posts = results.postResults.map { PostResponse.from(it) },
                totalCount = results.totalCount,
                hasNext = results.hasNext,
                nextPostId = results.nextPostId
            )
        }
    }
}
