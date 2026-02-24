package domain.repository;

import domain.entity.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {

    List<PostCategory> findAllByPostPostIdInAndCategoryIdIn(List<Long> postIds, List<Long> categoryIds);

    void deleteAllByPostPostIdIn(List<Long> postIds);
}
