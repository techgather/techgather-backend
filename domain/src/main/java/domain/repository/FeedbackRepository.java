package domain.repository;

import domain.constants.FeedbackCategory;
import domain.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    @Query("""
            SELECT f FROM Feedback f
            WHERE (:category IS NULL OR f.category = :category)
            ORDER BY f.createdAt DESC, f.id DESC
            """)
    List<Feedback> findAllForAdmin(@Param("category") FeedbackCategory category);
}
