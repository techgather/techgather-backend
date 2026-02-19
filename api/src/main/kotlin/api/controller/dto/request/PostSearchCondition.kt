package api.controller.dto.request

import domain.constants.Status
import jakarta.validation.constraints.Size

data class PostSearchCondition(

    @field:Size(min = 2, message = "검색 키워드는 최소 2글자 이상이어야 합니다.")
    val keyword: String? = null,
    val status: Status? = null,
)
