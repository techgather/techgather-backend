package api.event

import application.notification.DiscordNotifier
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class FeedbackCreatedNotificationListener(
    @Qualifier("feedbackDiscordNotifier")
    private val discordNotifier: DiscordNotifier
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun notify(event: FeedbackCreatedEvent) {
        discordNotifier.send(
            "📣 새 피드백 등록",
            buildString {
                appendLine("피드백 ID: ${event.feedbackId}")
                appendLine("카테고리: ${event.category} (${event.category.description})")
                append("내용: ${event.content}")
            }
        )
    }
}
