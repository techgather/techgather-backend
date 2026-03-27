package api.controller

import api.controller.dto.response.CategoryGroupResponse
import api.controller.dto.response.CategoryResponse
import api.service.CategoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/categories")
@Tag(name = "1-2 User Categories", description = "User category read APIs")
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping("/groups")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "카테고리 그룹 목록 조회", operationId = "u1-cat-groups")
    fun getCategoryGroups(): List<CategoryGroupResponse> {
        return categoryService.getCategoryGroups().map { CategoryGroupResponse.from(it) }
    }

    @GetMapping("/groups/{categoryGroupId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "카테고리 그룹 단건 조회", operationId = "u2-cat-group")
    fun getCategoryGroup(
        @PathVariable categoryGroupId: String
    ): CategoryGroupResponse {
        return CategoryGroupResponse.from(categoryService.getCategoryGroup(categoryGroupId))
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "카테고리 목록 조회", operationId = "u3-cat-list")
    fun getCategories(
        @RequestParam(required = false) categoryGroupId: String?
    ): List<CategoryResponse> {
        return categoryService.getCategories(categoryGroupId).map { CategoryResponse.from(it) }
    }

    @GetMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "카테고리 단건 조회", operationId = "u4-cat-get")
    fun getCategory(
        @PathVariable categoryId: String
    ): CategoryResponse {
        return CategoryResponse.from(categoryService.getCategory(categoryId))
    }

    @GetMapping("/slug/{slug}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "카테고리 slug 단건 조회", operationId = "u5-cat-get-by-slug")
    fun getCategoryBySlug(
        @PathVariable slug: String
    ): CategoryResponse {
        return CategoryResponse.from(categoryService.getCategoryBySlug(slug))
    }
}
