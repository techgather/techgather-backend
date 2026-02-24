package api.controller.dto.request

import domain.constants.PostStatus

data class UpdatePostsRequest(

    val postIds: List<String>,
    val status: PostStatus,
    val categoryIds: List<String>? = null
)
