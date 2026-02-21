package domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "category_group", indexes = {
        @Index(name = "idx_category_group_name", columnList = "name", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryGroup {

    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "categoryGroup", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Category> categories = new HashSet<>();

    public static CategoryGroup create(Long id, String name) {
        CategoryGroup categoryGroup = new CategoryGroup();
        categoryGroup.id = id;
        categoryGroup.name = name;
        return categoryGroup;
    }
}
