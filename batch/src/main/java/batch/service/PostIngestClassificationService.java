package batch.service;

import application.generator.SnowFlake;
import domain.constants.PostStatus;
import domain.entity.Category;
import domain.entity.Post;
import domain.entity.PostCategory;
import domain.repository.CategoryRepository;
import domain.repository.PostCategoryRepository;
import domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostIngestClassificationService {

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final NvidiaPostClassifier postClassifier;
    private final SnowFlake snowflake = SnowFlake.getInstance();

    public void classifyOrMoveToOnHold(Post post, String description, Collection<String> tags) {
        Map<String, Category> categoryBySlug = categoryRepository.findAllByOrderByNameAsc().stream()
                .collect(Collectors.toMap(Category::getSlug, Function.identity()));
        Optional<Category> category = postClassifier.classify(post, description, tags, categoryBySlug);

        if (category.isEmpty()) {
            postRepository.updateStatusByPostId(java.util.List.of(post.getPostId()), PostStatus.ON_HOLD);
            log.info("[적재 분류] 분류할 수 없어 게시글을 보류했습니다. postId={}", post.getPostId());
            return;
        }

        Post managedPost = postRepository.getReferenceById(post.getPostId());
        postCategoryRepository.save(PostCategory.create(
                snowflake.nextId(),
                managedPost,
                category.get()
        ));
        postRepository.updateStatusByPostId(java.util.List.of(post.getPostId()), PostStatus.RESERVED);
        log.info("[적재 분류] 분류된 게시글을 예약 상태로 변경했습니다. postId={}, category={}",
                post.getPostId(), category.get().getSlug());
    }
}
