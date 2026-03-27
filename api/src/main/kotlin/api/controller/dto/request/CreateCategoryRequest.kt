package api.controller.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateCategoryRequest(
    @field:NotBlank(message = "카테고리 그룹 ID는 필수입니다.")
    val categoryGroupId: String,

    @field:NotBlank(message = "카테고리 ID는 필수입니다.")
    val categoryId: String,

    @field:NotBlank(message = "카테고리 이름은 필수입니다.")
    @field:Size(max = 100, message = "카테고리 이름은 100자 이하여야 합니다.")
    val name: String,

    @field:NotBlank(message = "카테고리 slug는 필수입니다.")
    @field:Size(max = 120, message = "카테고리 slug는 120자 이하여야 합니다.")
    @field:Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "카테고리 slug 형식이 올바르지 않습니다.")
    val slug: String,

    @field:NotBlank(message = "카테고리 설명은 필수입니다.")
    @field:Size(max = 500, message = "카테고리 설명은 500자 이하여야 합니다.")
    val description: String
)
