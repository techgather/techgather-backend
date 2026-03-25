package api.controller.dto.response

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import domain.entity.Category

data class CategoryResponse(
    @field:JsonSerialize(using = ToStringSerializer::class)
    val id: Long,
    val name: String,
    @field:JsonSerialize(using = ToStringSerializer::class)
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
