// RESERVED 상태 게시글을 pub_date 오래된 순으로 정한 개수만큼 PUBLISHED로 전환하는 서비스
package batch.service;

import application.notification.AdminPostLinkFactory;
import application.notification.DiscordNotification;
import application.notification.DiscordNotifier;
import domain.constants.PostStatus;
import domain.entity.Post;
import domain.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PostReleaseService {

    private final PostRepository postRepository;
    private final DiscordNotifier discordNotifier;
    private final AdminPostLinkFactory adminPostLinkFactory;
    private final int dailyCount;

    public PostReleaseService(PostRepository postRepository, int dailyCount) {
        this(
                postRepository,
                new DiscordNotifier(false, "", 5000),
                new AdminPostLinkFactory(""),
                dailyCount
        );
    }

    @Autowired
    public PostReleaseService(PostRepository postRepository,
                              DiscordNotifier discordNotifier,
                              AdminPostLinkFactory adminPostLinkFactory,
                              @Value("${post.release.daily-count:5}") int dailyCount) {
        this.postRepository = postRepository;
        this.discordNotifier = discordNotifier;
        this.adminPostLinkFactory = adminPostLinkFactory;
        this.dailyCount = dailyCount;
    }

    @Transactional
    public void releaseReservedPosts() {
        List<Post> targets = postRepository.findByStatusOrderByPubDateAsc(PostStatus.RESERVED, PageRequest.of(0, dailyCount));
        if (targets.isEmpty()) {
            log.info("[공개] RESERVED 게시글 없음");
            return;
        }

        List<Long> postIds = targets.stream().map(Post::getPostId).toList();
        DiscordNotification releaseNotification = buildReleaseNotification(targets);
        postRepository.updateStatusByPostId(postIds, PostStatus.PUBLISHED);

        log.info("[공개] RESERVED→PUBLISHED {}건 전환", postIds.size());
        notifyReleaseAfterCommit(releaseNotification);
    }

    private void notifyReleaseAfterCommit(DiscordNotification releaseNotification) {
        Runnable notification = () -> discordNotifier.send(releaseNotification);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            notification.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notification.run();
            }
        });
    }

    private DiscordNotification buildReleaseNotification(List<Post> targets) {
        String postLinks = targets.stream()
                .limit(10)
                .map(post -> "• " + adminPostLinkFactory.markdownLink(post.getTitle(), post.getPostId()))
                .collect(Collectors.joining("\n"));
        if (targets.size() > 10) {
            postLinks += "\n• 외 " + (targets.size() - 10) + "건";
        }

        String categories = targets.stream()
                .flatMap(post -> post.getPostCategories().stream())
                .map(postCategory -> postCategory.getCategory().getName())
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));

        DiscordNotification.Builder builder = DiscordNotification
                .builder(DiscordNotification.Severity.SUCCESS, "예약 게시글 공개 완료")
                .description("예약 게시글이 공개 상태로 전환되었습니다.")
                .field("변경", targets.size() + "건", true)
                .field("상태", "예약 → 발행", true)
                .field("공개 게시글", postLinks)
                .footer("TechGather · batch");
        if (!categories.isBlank()) {
            builder.field("카테고리", categories);
        }
        return builder.build();
    }
}
