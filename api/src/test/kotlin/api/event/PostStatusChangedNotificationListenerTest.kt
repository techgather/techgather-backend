package api.event

import application.notification.AdminPostLinkFactory
import application.notification.DiscordNotification
import application.notification.DiscordNotifier
import domain.constants.PostStatus
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PostStatusChangedNotificationListenerTest {

    @Test
    fun `카테고리 이름과 관리자 게시글 링크를 알림에 포함한다`() {
        val notifier = mock(DiscordNotifier::class.java)
        val listener = PostStatusChangedNotificationListener(
            notifier,
            AdminPostLinkFactory("https://admin.example.com/posts/{postId}")
        )
        val event = PostStatusChangedEvent(
            requestedPostIds = listOf(1L, 2L),
            changedPosts = listOf(
                PostNotificationItem(1L, "첫 번째 게시글"),
                PostNotificationItem(2L, "두 번째 게시글")
            ),
            status = PostStatus.RESERVED,
            categoryNames = listOf("백엔드", "AI")
        )

        listener.notify(event)

        val captor = ArgumentCaptor.forClass(DiscordNotification::class.java)
        verify(notifier).send(captor.capture())
        val notification = captor.value

        assertEquals("게시글 상태 변경", notification.title())
        assertEquals(DiscordNotification.Severity.INFO, notification.severity())
        assertTrue(notification.fields().any {
            it.name() == "카테고리" && it.value() == "백엔드, AI"
        })
        assertTrue(notification.fields().any {
            it.name() == "변경 게시글" &&
                it.value().contains("[첫 번째 게시글](https://admin.example.com/posts/1)") &&
                it.value().contains("[두 번째 게시글](https://admin.example.com/posts/2)")
        })
    }
}
