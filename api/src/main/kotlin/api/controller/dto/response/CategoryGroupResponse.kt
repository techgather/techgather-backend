package api.controller.dto.response

import domain.entity.CategoryGroup

data class CategoryGroupResponse(
    val id: Long,
    val name: String
) {
    companion object {
        fun from(categoryGroup: CategoryGroup): CategoryGroupResponse {
            return CategoryGroupResponse(
                id = categoryGroup.id,
                name = categoryGroup.name
            )
        }
    }
}
