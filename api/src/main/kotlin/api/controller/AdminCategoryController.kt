package api.controller

import api.controller.dto.request.CreateCategoryGroupRequest
import api.controller.dto.request.CreateCategoryRequest
import api.controller.dto.request.UpdateCategoryGroupRequest
import api.controller.dto.request.UpdateCategoryRequest
import api.controller.dto.response.CategoryGroupResponse
import api.controller.dto.response.CategoryResponse
import api.service.CategoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/categories")
@Tag(name = "2-2 Admin Categories", description = "Admin category CRUD APIs")
class AdminCategoryController(
    private val categoryService: CategoryService
) {

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "카테고리 그룹 생성", operationId = "a1-group-create")
    fun createCategoryGroup(
        @Valid @RequestBody request: CreateCategoryGroupRequest
    ): CategoryGroupResponse {
        return CategoryGroupResponse.from(categoryService.createCategoryGroup(request))
    }

    @GetMapping("/groups")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "카테고리 그룹 목록 조회", operationId = "a2-group-list")
    fun getCategoryGroups(): List<CategoryGroupResponse> {
        return categoryService.getCategoryGroups().map { CategoryGroupResponse.from(it) }
    }

    @GetMapping("/groups/{categoryGroupId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "카테고리 그룹 단건 조회", operationId = "a3-group-get")
    fun getCategoryGroup(
        @PathVariable categoryGroupId: Long
    ): CategoryGroupResponse {
        return CategoryGroupResponse.from(categoryService.getCategoryGroup(categoryGroupId))
    }

    @PutMapping("/groups/{categoryGroupId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "카테고리 그룹 수정", operationId = "a4-group-update")
    fun updateCategoryGroup(
        @PathVariable categoryGroupId: Long,
        @Valid @RequestBody request: UpdateCategoryGroupRequest
    ): CategoryGroupResponse {
        return CategoryGroupResponse.from(categoryService.updateCategoryGroup(categoryGroupId, request))
    }

    @DeleteMapping("/groups/{categoryGroupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "카테고리 그룹 삭제", operationId = "a5-group-delete")
    fun deleteCategoryGroup(
        @PathVariable categoryGroupId: Long
    ) {
        categoryService.deleteCategoryGroup(categoryGroupId)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "카테고리 생성", operationId = "a6-cat-create")
    fun createCategory(
        @Valid @RequestBody request: CreateCategoryRequest
    ): CategoryResponse {
        return CategoryResponse.from(categoryService.createCategory(request))
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "카테고리 목록 조회", operationId = "a7-cat-list")
    fun getCategories(
        @RequestParam(required = false) categoryGroupId: Long?
    ): List<CategoryResponse> {
        return categoryService.getCategories(categoryGroupId).map { CategoryResponse.from(it) }
    }

    @GetMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "카테고리 단건 조회", operationId = "a8-cat-get")
    fun getCategory(
        @PathVariable categoryId: Long
    ): CategoryResponse {
        return CategoryResponse.from(categoryService.getCategory(categoryId))
    }

    @PutMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "카테고리 수정", operationId = "a9-cat-update")
    fun updateCategory(
        @PathVariable categoryId: Long,
        @Valid @RequestBody request: UpdateCategoryRequest
    ): CategoryResponse {
        return CategoryResponse.from(categoryService.updateCategory(categoryId, request))
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "카테고리 삭제", operationId = "a10-cat-delete")
    fun deleteCategory(
        @PathVariable categoryId: Long
    ) {
        categoryService.deleteCategory(categoryId)
    }
}
