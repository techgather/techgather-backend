package api.controller.dto.request

import domain.constants.FeedbackCategory
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import io.swagger.v3.oas.annotations.media.Schema

data class CreateFeedbackRequest(
    @field:NotNull(message = "피드백 카테고리는 필수입니다.")
    @field:Schema(
        description = "BUG: 오류·장애 신고, CONTENT: 콘텐츠 관련 의견, FEATURE: 신규 기능 제안, UX: 사용성·화면 개선, ETC: 기타",
        allowableValues = ["BUG", "CONTENT", "FEATURE", "UX", "ETC"]
    )
    val category: FeedbackCategory?,

    @field:NotBlank(message = "피드백 내용은 필수입니다.")
    @field:Size(max = 5000, message = "피드백 내용은 5000자 이하여야 합니다.")
    val content: String
)
