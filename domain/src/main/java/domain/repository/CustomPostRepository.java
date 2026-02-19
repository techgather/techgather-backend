package domain.repository;

import domain.constants.Language;
import domain.constants.Status;
import domain.entity.Post;

import java.util.List;

public interface CustomPostRepository {

	List<Post> searchPosts(Language language, String keyword, Status status, Long limit);

	List<Post> searchPosts(Language language, String keyword, Status status, Long lastPostId, Long limit);
}
