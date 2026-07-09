package api.controller

import api.controller.dto.request.ClassifyPostsRequest
import api.controller.dto.request.PostSearchCondition
import api.controller.dto.request.UpdatePostsRequest
import api.controller.dto.response.ClassifyPostsResponse
import api.controller.dto.response.PostResponseList
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import api.service.PostAutoClassifyService
import api.service.PostService
import domain.constants.Language
import domain.constants.PostStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/posts")
@Tag(name = "2-1 Admin Posts", description = "Admin post management APIs")
class AdminPostController(
    private val postService: PostService,
    private val postAutoClassifyService: PostAutoClassifyService
) {

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "관리자 게시글 목록 조회", operationId = "a1-post-list")
    fun getDiscardedPosts(
        @Valid searchCondition: PostSearchCondition,
        @RequestParam(required = false) status: PostStatus?,
        @RequestParam(required = false) lastPostId: String?,
        @RequestParam(defaultValue = "20") limit: Long,
        @RequestParam(required = false) language: Language?
    ): PostResponseList {
        val resolvedCondition = when {
            searchCondition.categorySlugs == null -> searchCondition.copy(unclassified = true)
            searchCondition.categorySlugs.isEmpty() -> searchCondition.copy(categorySlugs = null)
            else -> searchCondition
        }
        val results = postService.getPosts(resolvedCondition, status, language, lastPostId, limit)
        return PostResponseList.from(results)
    }

    @PatchMapping
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "게시글 상태 변경", operationId = "a2-post-status")
    fun markedPostsStatus(
        @RequestBody request: UpdatePostsRequest
    ) {
        postService.markedPostStatus(request.postIds, request.status, request.categoryIds)
    }

    @PostMapping("/classify")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "게시글 자동 카테고리 분류", operationId = "a4-post-classify")
    fun classifyPosts(
        @RequestBody request: ClassifyPostsRequest
    ): ClassifyPostsResponse {
        return ClassifyPostsResponse.from(postAutoClassifyService.classifyPosts(request.postIds))
    }

    @GetMapping("/source-sites")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "관리자 소스 사이트 목록 조회", operationId = "a3-post-sources")
    fun getSourceSites(
        @RequestParam(required = false) language: Language?
    ): List<String> {
        return postService.getSourceSiteNamesForAdmin(language)
    }

}
