package batch.service;

import application.generator.SnowFlake;
import domain.constants.PostStatus;
import domain.entity.Category;
import domain.entity.Post;
import domain.entity.PostCategory;
import domain.entity.PostTag;
import domain.entity.Tag;
import domain.repository.CategoryRepository;
import domain.repository.PostCategoryRepository;
import domain.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostIngestClassificationService {

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final OpenAiPostClassifier postClassifier;
    private final TransactionTemplate transactionTemplate;
    private final int batchSize;
    private final SnowFlake snowflake = SnowFlake.getInstance();

    public PostIngestClassificationService(
            CategoryRepository categoryRepository,
            PostRepository postRepository,
            PostCategoryRepository postCategoryRepository,
            OpenAiPostClassifier postClassifier,
            PlatformTransactionManager transactionManager,
            @Value("${classification.batch-size:40}") int batchSize
    ) {
        this.categoryRepository = categoryRepository;
        this.postRepository = postRepository;
        this.postCategoryRepository = postCategoryRepository;
        this.postClassifier = postClassifier;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.batchSize = batchSize;
    }

    public void classifyUnclassifiedPosts() {
        if (!postClassifier.isAvailable()) {
            log.info("[적재 분류] LLM 분류가 비활성화되어 건너뜁니다.");
            return;
        }

        // LLM 응답이 느려 DB 커넥션을 오래 잡지 않도록 조회·LLM 호출·저장을 분리한다.
        List<Target> targets = transactionTemplate.execute(status ->
                postRepository.findNotPublishedUnclassifiedPosts(PageRequest.of(0, batchSize)).stream()
                        .map(Target::from)
                        .toList());
        if (targets == null || targets.isEmpty()) {
            return;
        }

        Map<String, Category> categoryBySlug = categoryRepository.findAllByOrderByNameAsc().stream()
                .collect(Collectors.toMap(Category::getSlug, Function.identity()));
        log.info("[적재 분류] 미분류 게시글 {}건 분류 시작", targets.size());
        targets.forEach(target -> classifyOrMoveToOnHold(target, categoryBySlug));
    }

    private void classifyOrMoveToOnHold(Target target, Map<String, Category> categoryBySlug) {
        Optional<Category> category;
        try {
            category = postClassifier.classify(target.post(), target.tags(), categoryBySlug);
        } catch (PostClassificationException e) {
            log.warn("[적재 분류] LLM 호출 실패로 다음 실행에서 재시도합니다. postId={}", target.post().getPostId(), e);
            return;
        }

        Long postId = target.post().getPostId();
        transactionTemplate.executeWithoutResult(status -> {
            if (category.isEmpty()) {
                postRepository.updateStatusByPostId(List.of(postId), PostStatus.ON_HOLD);
                log.info("[적재 분류] 분류할 수 없어 게시글을 보류했습니다. postId={}", postId);
                return;
            }

            postCategoryRepository.save(PostCategory.create(
                    snowflake.nextId(),
                    postRepository.getReferenceById(postId),
                    category.get()
            ));
            postRepository.updateStatusByPostId(List.of(postId), PostStatus.RESERVED);
            log.info("[적재 분류] 분류된 게시글을 예약 상태로 변경했습니다. postId={}, category={}",
                    postId, category.get().getSlug());
        });
    }

    private record Target(Post post, List<String> tags) {
        private static Target from(Post post) {
            List<String> tags = post.getPostTags().stream()
                    .map(PostTag::getTag)
                    .map(Tag::getName)
                    .toList();
            return new Target(post, tags);
        }
    }
}
