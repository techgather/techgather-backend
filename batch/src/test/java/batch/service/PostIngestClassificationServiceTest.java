package batch.service;

import domain.constants.PostStatus;
import domain.entity.Category;
import domain.entity.Post;
import domain.repository.CategoryRepository;
import domain.repository.PostCategoryRepository;
import domain.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostIngestClassificationServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostCategoryRepository postCategoryRepository;

    @Mock
    private NvidiaPostClassifier postClassifier;

    private PostIngestClassificationService service;

    @BeforeEach
    void setUp() {
        service = new PostIngestClassificationService(
                categoryRepository,
                postRepository,
                postCategoryRepository,
                postClassifier
        );
    }

    @Test
    void savesCategoryWhenAiClassificationSucceeds() {
        Post post = post();
        Category category = Category.create(2L, null, "백엔드", "backend", "백엔드 기술");
        Post managedPost = post();
        when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(List.of(category));
        when(postClassifier.classify(eq(post), eq("본문 요약"), eq(Set.of("spring")), any()))
                .thenReturn(Optional.of(category));
        when(postRepository.getReferenceById(post.getPostId())).thenReturn(managedPost);

        service.classifyOrMoveToOnHold(post, "본문 요약", Set.of("spring"));

        ArgumentCaptor<domain.entity.PostCategory> captor = ArgumentCaptor.forClass(domain.entity.PostCategory.class);
        verify(postCategoryRepository).save(captor.capture());
        assertEquals(managedPost, captor.getValue().getPost());
        assertEquals(category, captor.getValue().getCategory());
        verify(postRepository).updateStatusByPostId(List.of(post.getPostId()), PostStatus.RESERVED);
    }

    @Test
    void movesPostToOnHoldWhenAiCannotClassify() {
        Post post = post();
        Category category = Category.create(2L, null, "백엔드", "backend", "백엔드 기술");
        when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(List.of(category));
        when(postClassifier.classify(eq(post), eq("본문 요약"), eq(Set.of()), any())).thenReturn(Optional.empty());

        service.classifyOrMoveToOnHold(post, "본문 요약", Set.of());

        verify(postRepository).updateStatusByPostId(List.of(post.getPostId()), PostStatus.ON_HOLD);
        verify(postCategoryRepository, never()).save(any());
    }

    private Post post() {
        return Post.create(
                1L,
                "Spring Batch 적재",
                "https://example.com/post",
                LocalDateTime.now(),
                null,
                "example",
                "KO"
        );
    }
}
