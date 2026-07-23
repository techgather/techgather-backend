package api.controller

import api.controller.dto.response.FeedbackResponse
import api.service.FeedbackService
import domain.constants.FeedbackCategory
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/feedbacks")
@Tag(name = "2-3 Admin Feedbacks", description = "Admin feedback APIs")
class AdminFeedbackController(
    private val feedbackService: FeedbackService
) {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "관리자 피드백 목록 조회", operationId = "a12-feedback-list")
    fun getFeedbacks(
        @Parameter(description = "BUG: 오류·장애 신고, CONTENT: 콘텐츠 관련 의견, FEATURE: 신규 기능 제안, UX: 사용성·화면 개선, ETC: 기타")
        @RequestParam(required = false) category: FeedbackCategory?
    ): List<FeedbackResponse> {
        return feedbackService.getFeedbacks(category).map(FeedbackResponse::from)
    }
}
