package domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_category", indexes = {
        @Index(name = "idx_post_category_post_id", columnList = "post_id"),
        @Index(name = "idx_post_category_category_id", columnList = "category_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_post_category_post_category", columnNames = {"post_id", "category_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostCategory {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public static PostCategory create(Long id, Post post, Category category) {
        PostCategory postCategory = new PostCategory();
        postCategory.id = id;
        postCategory.post = post;
        postCategory.category = category;
        return postCategory;
    }
}
