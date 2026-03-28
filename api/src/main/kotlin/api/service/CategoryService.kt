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
import java.util.Locale

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
        val categoryGroupId = parseId(request.categoryGroupId, "categoryGroupId")
        val categoryGroup = categoryGroupRepository.findById(categoryGroupId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리 그룹을 찾을 수 없습니다.") }
        
        val name = request.name.trim()
        val slug = normalizeSlug(request.slug)
        val description = request.description.trim()
        if (categoryRepository.existsByCategoryGroupIdAndName(categoryGroup.id, name)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 카테고리 이름입니다.")
        }
        if (categoryRepository.existsBySlug(slug)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 카테고리 slug입니다.")
        }

        return categoryRepository.save(Category.create(snowFlake.nextId(), categoryGroup, name, slug, description))
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

    @Transactional(readOnly = true)
    fun getCategoryBySlug(slug: String): Category {
        return categoryRepository.findBySlug(normalizeSlug(slug))
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다.") }
    }

    @Transactional
    fun updateCategory(categoryId: String, request: UpdateCategoryRequest): Category {
        val category = categoryRepository.findById(parseId(categoryId, "categoryId"))
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다.") }

        val name = request.name.trim()
        val slug = normalizeSlug(request.slug)
        val description = request.description.trim()
        if (category.name != name && categoryRepository.existsByCategoryGroupIdAndName(category.categoryGroup.id, name)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 카테고리 이름입니다.")
        }
        if (category.slug != slug && categoryRepository.existsBySlug(slug)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 카테고리 slug입니다.")
        }

        category.changeName(name)
        category.changeSlug(slug)
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

    private fun normalizeSlug(raw: String): String {
        val normalized = raw.trim().lowercase(Locale.ROOT)
        val slugPattern = Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$")
        if (!slugPattern.matches(normalized)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid slug format: $raw")
        }
        return normalized
    }
}
