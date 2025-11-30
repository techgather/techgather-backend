package api.controller.dto.request

import jakarta.validation.constraints.Size

data class TagFilterCondition(

    @field:Size(message = "태그는 최대 5개까지 선택할 수 있습니다.", max = 5)
    var tags: List<String> = emptyList()
)