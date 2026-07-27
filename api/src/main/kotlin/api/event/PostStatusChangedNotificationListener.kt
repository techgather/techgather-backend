package api.event

import application.notification.AdminPostLinkFactory
import application.notification.DiscordNotification
import application.notification.DiscordNotification.Severity
import application.notification.DiscordNotifier
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PostStatusChangedNotificationListener(
    private val discordNotifier: DiscordNotifier,
    private val adminPostLinkFactory: AdminPostLinkFactory
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun notify(event: PostStatusChangedEvent) {
        if (event.status != domain.constants.PostStatus.PUBLISHED || event.changedPosts.isEmpty()) {
            return
        }

        val categorySummary = event.categoryNames
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString(", ")
            ?: "기존 카테고리 유지"
        val postLinks = event.changedPosts
            .take(10)
            .joinToString("\n") { "• ${adminPostLinkFactory.markdownLink(it.title, it.id)}" }
            .let {
                if (event.changedPosts.size > 10) {
                    "$it\n• 외 ${event.changedPosts.size - 10}건"
                } else {
                    it
                }
            }

        discordNotifier.send(
            DiscordNotification.builder(Severity.INFO, "게시글 상태 변경")
                .description("관리자 요청으로 게시글 상태가 변경되었습니다.")
                .field("요청", "${event.requestedPostIds.size}건", true)
                .field("변경", "${event.changedPosts.size}건", true)
                .field("상태", event.status.displayName(), true)
                .field("카테고리", categorySummary)
                .field("변경 게시글", postLinks)
                .footer("TechGather · api")
                .build()
        )
    }

    private fun domain.constants.PostStatus.displayName(): String = when (this) {
        domain.constants.PostStatus.DISCARDED -> "제외"
        domain.constants.PostStatus.NOT_PUBLISHED -> "미발행"
        domain.constants.PostStatus.PUBLISHED -> "발행"
        domain.constants.PostStatus.ON_HOLD -> "보류"
        domain.constants.PostStatus.RESERVED -> "예약"
    }
}
