package domain.repository;

import domain.constants.Language;
import domain.constants.PostStatus;
import domain.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, CustomPostRepository {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.status = :status WHERE p.postId IN :postIds")
    void updateStatusByPostId(@Param("postIds") List<Long> postIds,
                              @Param("status") PostStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Post p WHERE p.status = :status")
    void markedPostsStatus(@Param("status") PostStatus status);

    List<Post> findAllByCreatedAtBefore(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT p.postId FROM Post p WHERE p.postId IN :postIds")
    List<Long> findPostByPostIdIn(@Param("postIds") List<Long> postIds);

    @Query("""
            SELECT DISTINCT p.sourceSiteName
            FROM Post p
            WHERE p.sourceSiteName IS NOT NULL
              AND (:status IS NULL OR p.status = :status)
              AND p.language = :language
            ORDER BY p.sourceSiteName ASC
            """)
    List<String> findDistinctSourceSiteNames(@Param("status") PostStatus status,
                                             @Param("language") Language language);

    @Query("""
            SELECT p FROM Post p
            WHERE p.status = 'PUBLISHED'
              AND NOT EXISTS (
                  SELECT pc FROM PostCategory pc WHERE pc.post.postId = p.postId
              )
            """)
    List<Post> findPublishedUnclassifiedPosts();

    @Query("SELECT p FROM Post p WHERE p.status = :status ORDER BY p.pubDate ASC")
    List<Post> findByStatusOrderByPubDateAsc(@Param("status") PostStatus status, Pageable pageable);
}
