// RESERVED 상태 게시글을 pub_date 오래된 순으로 정한 개수만큼 PUBLISHED로 전환하는 서비스
package batch.service;

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

import java.util.List;

@Slf4j
@Service
public class PostReleaseService {

    private final PostRepository postRepository;
    private final DiscordNotifier discordNotifier;
    private final int dailyCount;

    public PostReleaseService(PostRepository postRepository, int dailyCount) {
        this(postRepository, new DiscordNotifier(false, "", 5000), dailyCount);
    }

    @Autowired
    public PostReleaseService(PostRepository postRepository,
                              DiscordNotifier discordNotifier,
                              @Value("${post.release.daily-count:5}") int dailyCount) {
        this.postRepository = postRepository;
        this.discordNotifier = discordNotifier;
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
        postRepository.updateStatusByPostId(postIds, PostStatus.PUBLISHED);

        log.info("[공개] RESERVED→PUBLISHED {}건 전환", postIds.size());
        discordNotifier.send(
                "📝 게시글 상태 변경",
                "변경 게시글: " + postIds.size() + "건\n" +
                        "상태: RESERVED → PUBLISHED\n" +
                        "게시글 ID: " + postIds
        );
    }
}
