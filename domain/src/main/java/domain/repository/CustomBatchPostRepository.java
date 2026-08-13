package domain.repository;

import domain.entity.Post;

import java.util.List;

public interface CustomBatchPostRepository {

	int saveAllPost(List<Post> posts);

	List<String> findExistingUrls(List<String> urls);
}
