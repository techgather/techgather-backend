package domain.repository;

import domain.constants.Language;
import domain.constants.PostStatus;
import domain.entity.Post;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomPostRepository {

	List<Post> searchPosts(Language language, String keyword, List<String> categorySlugs, List<String> sourceSiteNames, PostStatus status, Long limit);

	List<Post> searchPosts(Language language, String keyword, List<String> categorySlugs, List<String> sourceSiteNames, PostStatus status, LocalDateTime lastPubDate, Long lastPostId, Long limit);

	long countPosts(Language language, String keyword, List<String> categorySlugs, List<String> sourceSiteNames, PostStatus status);
}
