package api.controller

import api.annotation.Role
import api.controller.dto.request.PostSearchCondition
import api.controller.dto.response.PostResponseList
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import api.service.PostService
import domain.constants.Language
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/posts")
class PostController(
    private val postService: PostService
) {

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    fun getPublishedPosts(
        @Valid searchCondition: PostSearchCondition,
        @RequestParam(required = false) lastPostId: Long?,
        @RequestParam(defaultValue = "20") limit: Long,
        @RequestParam(required = false) language: Language?
    ): PostResponseList {
        val results = postService.getPosts(searchCondition, language, lastPostId, limit)
        return PostResponseList.from(results)
    }

}

