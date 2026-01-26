package domain.repository;

import domain.constants.Status;
import domain.entity.Post;
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
                              @Param("status") Status status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Post p WHERE p.status = :status")
    void markedPostsStatus(@Param("status") Status status);

    List<Post> findAllByCreatedAtBefore(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT p.postId FROM Post p WHERE p.postId IN :postIds")
    List<Long> findPostByPostIdIn(@Param("postIds") List<Long> postIds);
}