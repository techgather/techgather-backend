package api.controller.dto.response

import domain.constants.FeedbackCategory

data class FeedbackCategoryResponse(
    val code: FeedbackCategory,
    val description: String
) {
    companion object {
        fun from(category: FeedbackCategory): FeedbackCategoryResponse {
            return FeedbackCategoryResponse(
                code = category,
                description = category.description
            )
        }
    }
}
