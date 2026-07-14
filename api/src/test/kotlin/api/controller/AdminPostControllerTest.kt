package api.controller

import api.controller.dto.request.ClassifyPostsRequest
import api.service.PostAutoClassifyAsyncService
import api.service.PostAutoClassifyService
import api.service.PostService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals

class AdminPostControllerTest {

    private val postService = mock(PostService::class.java)
    private val postAutoClassifyService = mock(PostAutoClassifyService::class.java)
    private val postAutoClassifyAsyncService = mock(PostAutoClassifyAsyncService::class.java)
    private val controller = AdminPostController(
        postService,
        postAutoClassifyService,
        postAutoClassifyAsyncService
    )

    @Test
    fun `게시글 ID를 정규화한 뒤 비동기 분류를 접수한다`() {
        val request = ClassifyPostsRequest(listOf("001", "2", "2"))
        val normalizedPostIds = listOf("1", "2")
        `when`(postAutoClassifyService.normalizePostIds(request.postIds)).thenReturn(normalizedPostIds)

        val response = controller.classifyPosts(request)

        assertEquals("ACCEPTED", response.status)
        assertEquals(2, response.requested)
        assertEquals(normalizedPostIds, response.postIds)
        verify(postAutoClassifyAsyncService).classifyPosts(normalizedPostIds)
    }
}
