package domain.repository;

import domain.entity.Post;

import java.util.List;

public interface CustomPostRepository {

	void saveAllPost(List<Post> posts);
}
