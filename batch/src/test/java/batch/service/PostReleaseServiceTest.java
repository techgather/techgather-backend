// PostReleaseService 단위 테스트 — RESERVED→PUBLISHED 전환 로직 검증
package batch.service;

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

    @Captor
    private ArgumentCaptor<List<Long>> idsCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

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
}
