package domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "category", indexes = {
        @Index(name = "idx_category_group_id", columnList = "category_group_id"),
        @Index(name = "idx_category_name", columnList = "name"),
        @Index(name = "idx_category_slug", columnList = "slug", unique = true)
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_group_name", columnNames = {"category_group_id", "name"}),
        @UniqueConstraint(name = "uk_category_slug", columnNames = {"slug"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_group_id", nullable = false)
    private CategoryGroup categoryGroup;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 120)
    private String slug;

    @Column(name = "category_description", nullable = false, length = 500)
    private String description;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PostCategory> postCategories = new HashSet<>();

    public static Category create(Long id, CategoryGroup categoryGroup, String name, String slug, String description) {
        Category category = new Category();
        category.id = id;
        category.categoryGroup = categoryGroup;
        category.name = name;
        category.slug = slug;
        category.description = description;
        return category;
    }

    public void changeCategoryGroup(CategoryGroup categoryGroup) {
        this.categoryGroup = categoryGroup;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeSlug(String slug) {
        this.slug = slug;
    }

    public void changeDescription(String description) {
        this.description = description;
    }
}
