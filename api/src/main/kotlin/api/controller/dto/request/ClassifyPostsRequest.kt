package api.controller.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class ClassifyPostsRequest(
    @field:NotEmpty(message = "postIds는 비어 있을 수 없습니다.")
    val postIds: List<@NotBlank(message = "postIds에 빈 값이 포함되어 있습니다.") String>
)
