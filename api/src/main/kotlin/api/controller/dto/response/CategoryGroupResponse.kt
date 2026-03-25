package api.controller.dto.response

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import domain.entity.CategoryGroup

data class CategoryGroupResponse(
    @field:JsonSerialize(using = ToStringSerializer::class)
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
