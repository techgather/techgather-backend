package api.event

import application.notification.DiscordNotifier
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PostStatusChangedNotificationListener(
    private val discordNotifier: DiscordNotifier
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun notify(event: PostStatusChangedEvent) {
        if (event.changedPostIds.isEmpty()) {
            return
        }

        val categorySummary = event.categoryIds
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString(", ")
            ?: "기존 카테고리 유지"

        discordNotifier.send(
            "📝 게시글 상태 변경",
            buildString {
                appendLine("요청 게시글: ${event.requestedPostIds.size}건")
                appendLine("변경 게시글: ${event.changedPostIds.size}건")
                appendLine("상태: ${event.status}")
                appendLine("카테고리: $categorySummary")
                append("게시글 ID: ${event.changedPostIds.joinToString(", ")}")
            }
        )
    }
}
