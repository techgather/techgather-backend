package api.controller.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateCategoryRequest(
    @field:NotBlank(message = "카테고리 그룹 ID는 필수입니다.")
    val categoryGroupId: String,

    @field:NotBlank(message = "카테고리 이름은 필수입니다.")
    @field:Size(max = 100, message = "카테고리 이름은 100자 이하여야 합니다.")
    val name: String,

    @field:NotBlank(message = "카테고리 설명은 필수입니다.")
    @field:Size(max = 500, message = "카테고리 설명은 500자 이하여야 합니다.")
    val description: String
)
