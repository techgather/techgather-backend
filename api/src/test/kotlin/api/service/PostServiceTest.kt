package api.service

import api.event.PostStatusChangedEvent
import domain.constants.PostStatus
import domain.entity.Post
import domain.repository.CategoryRepository
import domain.repository.PostCategoryRepository
import domain.repository.PostRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.context.ApplicationEventPublisher
import kotlin.test.assertEquals

class PostServiceTest {

    private val postRepository = mock(PostRepository::class.java)
    private val categoryRepository = mock(CategoryRepository::class.java)
    private val postCategoryRepository = mock(PostCategoryRepository::class.java)
    private val eventPublisher = mock(ApplicationEventPublisher::class.java)
    private val postService = PostService(
        postRepository,
        categoryRepository,
        postCategoryRepository,
        eventPublisher
    )

    @Test
    fun `이미 PUBLISHED인 게시물은 상태 변경 알림 대상에서 제외한다`() {
        val publishedPost = post(1L, "기존 발행 게시글", PostStatus.PUBLISHED)
        val notPublishedPost = post(2L, "새 발행 게시글", PostStatus.NOT_PUBLISHED)
        `when`(postRepository.findPostByPostIdIn(listOf(1L, 2L))).thenReturn(listOf(1L, 2L))
        `when`(postRepository.findAllById(listOf(1L, 2L)))
            .thenReturn(listOf(publishedPost, notPublishedPost))

        postService.markedPostStatus(
            postIds = listOf("1", "2"),
            status = PostStatus.PUBLISHED
        )

        val eventCaptor = ArgumentCaptor.forClass(PostStatusChangedEvent::class.java)
        verify(eventPublisher).publishEvent(eventCaptor.capture())
        assertEquals(listOf(2L), eventCaptor.value.changedPosts.map { it.id })
    }

    private fun post(id: Long, title: String, status: PostStatus): Post {
        val post = mock(Post::class.java)
        `when`(post.postId).thenReturn(id)
        `when`(post.title).thenReturn(title)
        `when`(post.status).thenReturn(status)
        return post
    }
}
