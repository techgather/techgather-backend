package domain.repository;

import domain.constants.Language;
import domain.entity.Post;

import java.util.List;

public interface CustomPostRepository {

	List<Post> searchPosts(Language language, String keyword, Long limit);

	List<Post> searchPosts(Language language, String keyword, Long lastPostId, Long limit);
}
