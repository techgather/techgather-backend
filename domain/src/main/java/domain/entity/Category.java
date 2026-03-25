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
        @Index(name = "idx_category_name", columnList = "name")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_group_name", columnNames = {"category_group_id", "name"})
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

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PostCategory> postCategories = new HashSet<>();

    public static Category create(Long id, CategoryGroup categoryGroup, String name) {
        Category category = new Category();
        category.id = id;
        category.categoryGroup = categoryGroup;
        category.name = name;
        return category;
    }

    public void changeCategoryGroup(CategoryGroup categoryGroup) {
        this.categoryGroup = categoryGroup;
    }

    public void changeName(String name) {
        this.name = name;
    }
}
