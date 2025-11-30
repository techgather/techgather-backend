package domain.repository;

import domain.entity.Post;

import java.util.List;

public interface CustomPostRepository {

	List<Post> findPosts(Long limit);

	List<Post> findPosts(Long lastPostId, Long limit);

	List<Post> findPostByTag(List<String> tags, Long limit);

	List<Post> findPostByTag(List<String> tags, Long lastPostId, Long limit);

	List<Post> findPostByKeyword(String keyword, Long limit);

	List<Post> findPostByKeyword(String keyword, Long lastPostId, Long limit);
}
