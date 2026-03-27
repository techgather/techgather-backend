package domain.repository;

import domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByCategoryGroupIdAndName(Long categoryGroupId, String name);

    boolean existsBySlug(String slug);

    Optional<Category> findBySlug(String slug);

    List<Category> findAllByOrderByNameAsc();

    List<Category> findAllByCategoryGroupIdOrderByNameAsc(Long categoryGroupId);
}
