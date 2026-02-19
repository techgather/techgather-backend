package api.controller.dto.request

import domain.constants.PostStatus

data class UpdatePostStatusRequest(

    val postIds: List<Long>,
    val status: PostStatus
)
