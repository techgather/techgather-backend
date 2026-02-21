package domain.repository;

import domain.constants.Language;
import domain.constants.PostStatus;
import domain.entity.Post;

import java.util.List;

public interface CustomPostRepository {

	List<Post> searchPosts(Language language, String keyword, List<Long> categoryIds, String sourceSiteName, PostStatus status, Long limit);

	List<Post> searchPosts(Language language, String keyword, List<Long> categoryIds, String sourceSiteName, PostStatus status, Long lastPostId, Long limit);
}
