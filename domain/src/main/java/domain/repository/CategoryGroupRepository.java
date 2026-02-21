package domain.repository;

import domain.entity.CategoryGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryGroupRepository extends JpaRepository<CategoryGroup, Long> {

    boolean existsByName(String name);

    List<CategoryGroup> findAllByOrderByNameAsc();
}
