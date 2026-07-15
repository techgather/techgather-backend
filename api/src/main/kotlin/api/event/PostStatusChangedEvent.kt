package api.event

import domain.constants.PostStatus

data class PostStatusChangedEvent(
    val requestedPostIds: List<Long>,
    val changedPostIds: List<Long>,
    val status: PostStatus,
    val categoryIds: List<Long>?
)
