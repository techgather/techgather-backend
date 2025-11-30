package api.service

import api.controller.dto.request.PostSearchCondition
import api.controller.dto.request.TagFilterCondition
import api.service.dto.result.PostResults
import domain.entity.Post
import domain.repository.PostRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostService(
    private val postRepository: PostRepository
) {

    @Transactional(readOnly = true)
    fun getPosts(lastPostId: Long?, limit: Long): PostResults {
        val posts = when (lastPostId) {
            null -> postRepository.findPosts(limit + 1)
            else -> postRepository.findPosts(lastPostId, limit + 1)
        }
            .toList()
            .onEach { it.postTags.size }
        return createPostResults(posts, limit)
    }

    @Transactional(readOnly = true)
    fun filterPostByTag(tagFilterCondition: TagFilterCondition, lastPostId: Long?, limit: Long): PostResults {
        val posts = when (lastPostId) {
            null -> postRepository.findPostByTag(tagFilterCondition.tags, limit + 1)
            else -> postRepository.findPostByTag(tagFilterCondition.tags, lastPostId, limit + 1)
        }
            .toList()
            .onEach { it.postTags.size }
        return createPostResults(posts, limit)
    }

    @Transactional(readOnly = true)
    fun searchPost(postSearchCondition: PostSearchCondition, lastPostId: Long?, limit: Long): PostResults {
        val posts = when (lastPostId) {
            null -> postRepository.findPostByKeyword(postSearchCondition.keyword, limit + 1)
            else -> postRepository.findPostByKeyword(postSearchCondition.keyword, lastPostId, limit + 1)
        }
            .toList()
            .onEach { it.postTags.size }
        return createPostResults(posts, limit)
    }

    private fun createPostResults(posts: List<Post>, limit: Long): PostResults {
        val hasNext = posts.size > limit
        val (resultPosts, nextPostId) = when {
            hasNext -> posts.dropLast(1) to posts.last().postId
            else -> posts to null
        }

        return PostResults.of(
            posts = resultPosts,
            hasNext = hasNext,
            nextPostId = nextPostId
        )
    }
}

