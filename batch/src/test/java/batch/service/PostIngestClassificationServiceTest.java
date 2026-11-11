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
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
    private OpenAiPostClassifier postClassifier;

    @Mock
    private PlatformTransactionManager transactionManager;

    private PostIngestClassificationService service;

    @BeforeEach
    void setUp() {
        service = new PostIngestClassificationService(
                categoryRepository,
                postRepository,
                postCategoryRepository,
                postClassifier,
                transactionManager,
                40
        );
    }

    @Test
    void savesCategoryWhenAiClassificationSucceeds() {
        Post post = post();
        Category category = category();
        Post managedPost = post();
        givenUnclassifiedPosts(post, category);
        when(postClassifier.classify(eq(post), eq(List.of()), any())).thenReturn(Optional.of(category));
        when(postRepository.getReferenceById(post.getPostId())).thenReturn(managedPost);

        service.classifyUnclassifiedPosts();

        ArgumentCaptor<domain.entity.PostCategory> captor = ArgumentCaptor.forClass(domain.entity.PostCategory.class);
        verify(postCategoryRepository).save(captor.capture());
        assertEquals(managedPost, captor.getValue().getPost());
        assertEquals(category, captor.getValue().getCategory());
        verify(postRepository).updateStatusByPostId(List.of(post.getPostId()), PostStatus.RESERVED);
    }

    @Test
    void movesPostToOnHoldWhenAiCannotClassify() {
        Post post = post();
        givenUnclassifiedPosts(post, category());
        when(postClassifier.classify(eq(post), eq(List.of()), any())).thenReturn(Optional.empty());

        service.classifyUnclassifiedPosts();

        verify(postRepository).updateStatusByPostId(List.of(post.getPostId()), PostStatus.ON_HOLD);
        verify(postCategoryRepository, never()).save(any());
    }

    @Test
    void keepsPostUnclassifiedWhenAiCallFails() {
        Post post = post();
        givenUnclassifiedPosts(post, category());
        when(postClassifier.classify(eq(post), eq(List.of()), any()))
                .thenThrow(new PostClassificationException("timeout", new RuntimeException()));

        service.classifyUnclassifiedPosts();

        verify(postRepository, never()).updateStatusByPostId(anyList(), any());
        verify(postCategoryRepository, never()).save(any());
    }

    @Test
    void skipsWhenAiClassifierIsUnavailable() {
        when(postClassifier.isAvailable()).thenReturn(false);

        service.classifyUnclassifiedPosts();

        verify(postRepository, never()).findNotPublishedUnclassifiedPosts(any());
        verify(postRepository, never()).updateStatusByPostId(anyList(), any());
    }

    private void givenUnclassifiedPosts(Post post, Category category) {
        when(postClassifier.isAvailable()).thenReturn(true);
        when(postRepository.findNotPublishedUnclassifiedPosts(any(Pageable.class))).thenReturn(List.of(post));
        when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(List.of(category));
    }

    private Category category() {
        return Category.create(2L, null, "백엔드", "backend", "백엔드 기술");
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
