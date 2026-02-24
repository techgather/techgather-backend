package domain.repository;

import domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByCategoryGroupIdAndName(Long categoryGroupId, String name);

    List<Category> findAllByOrderByNameAsc();

    List<Category> findAllByCategoryGroupIdOrderByNameAsc(Long categoryGroupId);
}
