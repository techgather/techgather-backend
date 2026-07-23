package api.service

import api.controller.dto.request.CreateFeedbackRequest
import api.event.FeedbackCreatedEvent
import application.generator.SnowFlake
import domain.constants.FeedbackCategory
import domain.entity.Feedback
import domain.repository.FeedbackRepository
import org.springframework.stereotype.Service
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.annotation.Transactional

@Service
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    private val snowFlake = SnowFlake.getInstance()

    @Transactional
    fun createFeedback(request: CreateFeedbackRequest): Feedback {
        val feedback = feedbackRepository.save(
            Feedback.create(snowFlake.nextId(), request.category!!, request.content.trim())
        )
        applicationEventPublisher.publishEvent(
            FeedbackCreatedEvent(
                feedbackId = feedback.id,
                category = feedback.category,
                content = feedback.content
            )
        )
        return feedback
    }

    @Transactional(readOnly = true)
    fun getFeedbacks(category: FeedbackCategory?): List<Feedback> {
        return feedbackRepository.findAllForAdmin(category)
    }
}
