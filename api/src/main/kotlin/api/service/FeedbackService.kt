package api.service

import api.controller.dto.request.CreateFeedbackRequest
import application.generator.SnowFlake
import domain.constants.FeedbackCategory
import domain.entity.Feedback
import domain.repository.FeedbackRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeedbackService(
    private val feedbackRepository: FeedbackRepository
) {

    private val snowFlake = SnowFlake.getInstance()

    @Transactional
    fun createFeedback(request: CreateFeedbackRequest): Feedback {
        return feedbackRepository.save(
            Feedback.create(snowFlake.nextId(), request.category!!, request.content.trim())
        )
    }

    @Transactional(readOnly = true)
    fun getFeedbacks(category: FeedbackCategory?): List<Feedback> {
        return feedbackRepository.findAllForAdmin(category)
    }
}
