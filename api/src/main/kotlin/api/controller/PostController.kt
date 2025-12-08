package api.controller

import api.controller.dto.request.PostSearchCondition
import api.controller.dto.request.TagFilterCondition
import api.controller.dto.response.PostResponseList
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import api.service.PostService
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
    fun getPosts(
        @RequestParam(required = false) lastPostId: Long?,
        @RequestParam(defaultValue = "20") limit: Long
    ): PostResponseList {
        val results = postService.getPosts(lastPostId, limit)
        return PostResponseList.from(results)
    }

    @GetMapping("/filter")
    @ResponseStatus(code = HttpStatus.OK)
    fun filterPostByTag(
        @Valid tagFilterCondition: TagFilterCondition,
        @RequestParam(required = false) lastPostId: Long?,
        @RequestParam(defaultValue = "20") limit: Long
    ): PostResponseList {
        val results = postService.filterPostByTag(tagFilterCondition, lastPostId, limit)
        return PostResponseList.from(results)
    }

    @GetMapping("/search")
    @ResponseStatus(code = HttpStatus.OK)
    fun searchPost(
        @Valid postSearchCondition: PostSearchCondition,
        @RequestParam(required = false) lastPostId: Long?,
        @RequestParam(defaultValue = "20") limit: Long
    ): PostResponseList {
        val results = postService.searchPost(postSearchCondition, lastPostId, limit)
        return PostResponseList.from(results)
    }

}

