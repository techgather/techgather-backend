package api.controller.dto.response

import domain.entity.Feedback
import domain.constants.FeedbackCategory
import java.time.LocalDateTime

data class FeedbackResponse(
    val id: Long,
    val category: FeedbackCategory,
    val content: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(feedback: Feedback): FeedbackResponse {
            return FeedbackResponse(
                id = feedback.id,
                category = feedback.category,
                content = feedback.content,
                createdAt = feedback.createdAt
            )
        }
    }
}
