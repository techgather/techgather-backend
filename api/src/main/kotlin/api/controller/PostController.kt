package api.controller

import api.controller.dto.request.PostSearchCondition
import api.controller.dto.response.PostResponseList
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import api.service.PostService
import domain.constants.Language
import domain.constants.PostStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/posts")
@Tag(name = "1-1 User Posts", description = "User post read APIs")
class PostController(
    private val postService: PostService
) {

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "게시글 목록 조회", operationId = "u1-post-list")
    fun getPublishedPosts(
        @Valid searchCondition: PostSearchCondition,
        @RequestParam(required = false) lastPostId: Long?,
        @RequestParam(defaultValue = "20") limit: Long,
        @RequestParam(required = false) language: Language?
    ): PostResponseList {
        val results = postService.getPosts(searchCondition, PostStatus.PUBLISHED, language, lastPostId, limit)
        return PostResponseList.from(results)
    }

}
