package api.controller.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PostSearchCondition(

    @field:NotBlank(message = "검색 키워드를 입력해주세요.")
    @field:Size(min = 2, message = "검색 키워드는 최소 2글자 이상이어야 합니다.")
    var keyword: String = "",
)