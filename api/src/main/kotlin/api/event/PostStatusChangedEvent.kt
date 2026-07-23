package api.event

import domain.constants.PostStatus

data class PostStatusChangedEvent(
    val requestedPostIds: List<Long>,
    val changedPosts: List<PostNotificationItem>,
    val status: PostStatus,
    val categoryNames: List<String>?
)

data class PostNotificationItem(
    val id: Long,
    val title: String
)
