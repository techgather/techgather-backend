package api.controller.dto.response

import domain.entity.Category

data class CategoryResponse(
    val id: Long,
    val name: String,
    val categoryGroupId: Long,
    val categoryGroupName: String
) {
    companion object {
        fun from(category: Category): CategoryResponse {
            return CategoryResponse(
                id = category.id,
                name = category.name,
                categoryGroupId = category.categoryGroup.id,
                categoryGroupName = category.categoryGroup.name
            )
        }
    }
}
