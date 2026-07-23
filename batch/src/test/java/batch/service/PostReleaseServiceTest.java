// PostReleaseService 단위 테스트 — RESERVED→PUBLISHED 전환 로직 검증
package batch.service;

import application.notification.AdminPostLinkFactory;
import application.notification.DiscordNotification;
import application.notification.DiscordNotifier;
import domain.entity.Post;
import domain.constants.PostStatus;
import domain.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostReleaseServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private DiscordNotifier discordNotifier;

    @Captor
    private ArgumentCaptor<List<Long>> idsCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Captor
    private ArgumentCaptor<DiscordNotification> notificationCaptor;

    private Post postWithId(long id) {
        Post post = Post.create(id, "title-" + id, "https://ex.com/" + id,
                LocalDateTime.now(), null, "site", "KO");
        return post;
    }

    @Test
    @DisplayName("RESERVED 게시글을 dailyCount만큼 PUBLISHED로 전환한다")
    void releaseReservedPosts_transitionsToPublished() {
        PostReleaseService service = new PostReleaseService(postRepository, 5);
        List<Post> reserved = List.of(postWithId(1L), postWithId(2L), postWithId(3L));
        when(postRepository.findByStatusOrderByPubDateAsc(eq(PostStatus.RESERVED), any())).thenReturn(reserved);

        service.releaseReservedPosts();

        verify(postRepository).findByStatusOrderByPubDateAsc(eq(PostStatus.RESERVED), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue()).isEqualTo(PageRequest.of(0, 5));

        verify(postRepository).updateStatusByPostId(idsCaptor.capture(), eq(PostStatus.PUBLISHED));
        assertThat(idsCaptor.getValue()).containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("RESERVED 게시글이 없으면 상태 변경을 호출하지 않는다")
    void releaseReservedPosts_emptyDoesNothing() {
        PostReleaseService service = new PostReleaseService(postRepository, 5);
        when(postRepository.findByStatusOrderByPubDateAsc(eq(PostStatus.RESERVED), any())).thenReturn(List.of());

        service.releaseReservedPosts();

        verify(postRepository, never()).updateStatusByPostId(anyList(), any());
    }

    @Test
    @DisplayName("예약 공개 알림은 커밋 이후 게시글 제목과 관리자 링크를 포함해 전송한다")
    void releaseReservedPosts_notifiesAfterCommit() {
        PostReleaseService service = new PostReleaseService(
                postRepository,
                discordNotifier,
                new AdminPostLinkFactory("https://admin.example.com/posts/{postId}"),
                5
        );
        List<Post> reserved = List.of(postWithId(1L), postWithId(2L));
        when(postRepository.findByStatusOrderByPubDateAsc(eq(PostStatus.RESERVED), any()))
                .thenReturn(reserved);

        TransactionSynchronizationManager.initSynchronization();
        try {
            service.releaseReservedPosts();

            verify(discordNotifier, never()).send(any(DiscordNotification.class));

            TransactionSynchronizationUtils.triggerAfterCommit();

            verify(discordNotifier).send(notificationCaptor.capture());
            DiscordNotification notification = notificationCaptor.getValue();
            assertThat(notification.title()).isEqualTo("예약 게시글 공개 완료");
            assertThat(notification.fields())
                    .filteredOn(field -> "공개 게시글".equals(field.name()))
                    .singleElement()
                    .satisfies(field -> {
                        assertThat(field.value())
                                .contains("[title-1](https://admin.example.com/posts/1)")
                                .contains("[title-2](https://admin.example.com/posts/2)");
                    });
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
