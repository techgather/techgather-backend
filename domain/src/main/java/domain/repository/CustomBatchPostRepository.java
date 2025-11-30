package domain.repository;

import domain.entity.Post;

import java.util.List;

public interface CustomBatchPostRepository {

	void saveAllPost(List<Post> posts);
}
