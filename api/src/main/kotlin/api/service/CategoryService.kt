package api.service

import api.controller.dto.request.CreateCategoryGroupRequest
import api.controller.dto.request.CreateCategoryRequest
import api.controller.dto.request.UpdateCategoryGroupRequest
import api.controller.dto.request.UpdateCategoryRequest
import application.generator.SnowFlake
import domain.entity.Category
import domain.entity.CategoryGroup
import domain.repository.CategoryGroupRepository
import domain.repository.CategoryRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class CategoryService(
    private val categoryGroupRepository: CategoryGroupRepository,
    private val categoryRepository: CategoryRepository
) {

    private val snowFlake = SnowFlake.getInstance()

    @Transactional
    fun createCategoryGroup(request: CreateCategoryGroupRequest): CategoryGroup {
        val name = request.name.trim()
        if (categoryGroupRepository.existsByName(name)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 카테고리 그룹 이름입니다.")
        }

        return categoryGroupRepository.save(CategoryGroup.create(snowFlake.nextId(), name))
    }

    @Transactional(readOnly = true)
    fun getCategoryGroups(): List<CategoryGroup> {
        return categoryGroupRepository.findAllByOrderByNameAsc()
    }

    @Transactional(readOnly = true)
    fun getCategoryGroup(categoryGroupId: String): CategoryGroup {
        return categoryGroupRepository.findById(parseId(categoryGroupId, "categoryGroupId"))
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리 그룹을 찾을 수 없습니다.") }
    }

    @Transactional
    fun updateCategoryGroup(categoryGroupId: String, request: UpdateCategoryGroupRequest): CategoryGroup {
        val categoryGroup = categoryGroupRepository.findById(parseId(categoryGroupId, "categoryGroupId"))
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리 그룹을 찾을 수 없습니다.") }

        val name = request.name.trim()
        if (categoryGroup.name != name && categoryGroupRepository.existsByName(name)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 카테고리 그룹 이름입니다.")
        }

        categoryGroup.changeName(name)
        return categoryGroup
    }

    @Transactional
    fun deleteCategoryGroup(categoryGroupId: String) {
        val categoryGroup = categoryGroupRepository.findById(parseId(categoryGroupId, "categoryGroupId"))
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리 그룹을 찾을 수 없습니다.") }
        categoryGroupRepository.delete(categoryGroup)
    }

    @Transactional
    fun createCategory(request: CreateCategoryRequest): Category {
        val categoryId = parseId(request.categoryId, "categoryId")
        val categoryGroupId = parseId(request.categoryGroupId, "categoryGroupId")
        val categoryGroup = categoryGroupRepository.findById(categoryGroupId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리 그룹을 찾을 수 없습니다.") }

        if (categoryRepository.existsById(categoryId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 카테고리 ID입니다.")
        }

        val name = request.name.trim()
        val description = request.description.trim()
        if (categoryRepository.existsByCategoryGroupIdAndName(categoryGroup.id, name)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 카테고리 이름입니다.")
        }

        return categoryRepository.save(Category.create(categoryId, categoryGroup, name, description))
    }

    @Transactional(readOnly = true)
    fun getCategories(categoryGroupId: String?): List<Category> {
        val parsedCategoryGroupId = parseNullableId(categoryGroupId, "categoryGroupId")
        return if (parsedCategoryGroupId == null) {
            categoryRepository.findAllByOrderByNameAsc()
        } else {
            categoryRepository.findAllByCategoryGroupIdOrderByNameAsc(parsedCategoryGroupId)
        }
    }

    @Transactional(readOnly = true)
    fun getCategory(categoryId: String): Category {
        return categoryRepository.findById(parseId(categoryId, "categoryId"))
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다.") }
    }

    @Transactional
    fun updateCategory(categoryId: String, request: UpdateCategoryRequest): Category {
        val category = categoryRepository.findById(parseId(categoryId, "categoryId"))
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다.") }

        val categoryGroupId = parseId(request.categoryGroupId, "categoryGroupId")
        val categoryGroup = categoryGroupRepository.findById(categoryGroupId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리 그룹을 찾을 수 없습니다.") }

        val name = request.name.trim()
        val description = request.description.trim()
        val isSameGroup = category.categoryGroup.id == categoryGroup.id
        val isSameName = category.name == name
        if ((!isSameGroup || !isSameName) && categoryRepository.existsByCategoryGroupIdAndName(categoryGroup.id, name)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 카테고리 이름입니다.")
        }

        category.changeCategoryGroup(categoryGroup)
        category.changeName(name)
        category.changeDescription(description)
        return category
    }

    @Transactional
    fun deleteCategory(categoryId: String) {
        val category = categoryRepository.findById(parseId(categoryId, "categoryId"))
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다.") }
        categoryRepository.delete(category)
    }

    private fun parseNullableId(id: String?, fieldName: String): Long? {
        if (id == null) {
            return null
        }
        return parseId(id, fieldName)
    }

    private fun parseId(id: String, fieldName: String): Long {
        return id.toLongOrNull()
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid $fieldName: $id")
    }
}
