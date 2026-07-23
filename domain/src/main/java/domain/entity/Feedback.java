package domain.entity;

import domain.constants.FeedbackCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback", indexes = {
        @Index(name = "idx_feedback_category_id", columnList = "category, id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feedback {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackCategory category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Feedback create(Long id, FeedbackCategory category, String content) {
        Feedback feedback = new Feedback();
        feedback.id = id;
        feedback.category = category;
        feedback.content = content;
        feedback.createdAt = LocalDateTime.now();
        return feedback;
    }
}
