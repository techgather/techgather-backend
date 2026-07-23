package api.controller

import api.controller.dto.request.CreateFeedbackRequest
import api.controller.dto.response.FeedbackResponse
import api.service.FeedbackService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/feedbacks")
@Tag(name = "1-2 User Feedbacks", description = "User feedback APIs")
class FeedbackController(
    private val feedbackService: FeedbackService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "피드백 생성", operationId = "u3-feedback-create")
    fun createFeedback(
        @Valid @RequestBody request: CreateFeedbackRequest
    ): FeedbackResponse {
        return FeedbackResponse.from(feedbackService.createFeedback(request))
    }
}
