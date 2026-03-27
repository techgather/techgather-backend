package api.controller.dto.request

import jakarta.validation.constraints.Pattern

data class PostSearchCondition(

    @field:Pattern(regexp = "^$|.{2,}$", message = "검색 키워드는 최소 2글자 이상이어야 합니다.")
    val keyword: String? = null,

    val categorySlugs: List<String>? = null,

    val sourceSiteNames: List<String>? = null,
)
