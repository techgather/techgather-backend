package api.controller.dto.request

import domain.constants.PostStatus

data class UpdatePostsRequest(

    val postIds: List<Long>,
    val status: PostStatus,
    val categoryIds: List<Long>? = null
)
