package api.event

import domain.constants.FeedbackCategory

data class FeedbackCreatedEvent(
    val feedbackId: Long,
    val category: FeedbackCategory,
    val content: String
)
