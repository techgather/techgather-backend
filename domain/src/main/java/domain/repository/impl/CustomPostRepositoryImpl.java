package domain.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import domain.entity.Post;
import domain.repository.CustomPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static domain.entity.QPost.post;
import static domain.entity.QPostTag.postTag;
import static domain.entity.QTag.tag;

@Repository
@RequiredArgsConstructor
public class CustomPostRepositoryImpl implements CustomPostRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Post> findPosts(Long limit) {
		List<Long> postIds = queryFactory
				.select(post.postId)
				.from(post)
				.orderBy(post.postId.desc())
				.limit(limit+1)
				.fetch();
		return queryFactory
				.selectFrom(post)
				.distinct()
				.leftJoin(post.postTags, postTag).fetchJoin()
				.leftJoin(postTag.tag, tag).fetchJoin()
				.where(post.postId.in(postIds))
				.orderBy(post.postId.desc())
				.fetch();
	}

	@Override
	public List<Post> findPosts(Long lastPostId, Long limit) {
		List<Long> postIds = queryFactory
				.select(post.postId)
				.from(post)
				.where(ltPostId(lastPostId))
				.orderBy(post.postId.desc())
				.limit(limit+1)
				.fetch();
		return queryFactory
				.selectFrom(post)
				.distinct()
				.leftJoin(post.postTags, postTag).fetchJoin()
				.leftJoin(postTag.tag, tag).fetchJoin()
				.where(post.postId.in(postIds))
				.orderBy(post.postId.desc())
				.fetch();
	}

	@Override
	public List<Post> findPostByTag(List<String> tags, Long limit) {
		List<Long> postIds = queryFactory
				.select(post.postId)
				.from(post)
				.join(post.postTags, postTag)
				.join(postTag.tag, tag)
				.where(inTagNames(tags))
				.orderBy(post.postId.desc())
				.distinct()
				.limit(limit+1)
				.fetch();
		return queryFactory
				.selectFrom(post)
				.distinct()
				.leftJoin(post.postTags, postTag).fetchJoin()
				.leftJoin(postTag.tag, tag).fetchJoin()
				.where(post.postId.in(postIds))
				.orderBy(post.postId.desc())
				.fetch();
	}

	@Override
	public List<Post> findPostByTag(List<String> tags, Long lastPostId, Long limit) {
		List<Long> postIds = queryFactory
				.select(post.postId)
				.from(post)
				.join(post.postTags, postTag)
				.join(postTag.tag, tag)
				.where(
					inTagNames(tags),
					ltPostId(lastPostId)
				)
				.orderBy(post.postId.desc())
				.distinct()
				.limit(limit+1)
				.fetch();
		return queryFactory
				.selectFrom(post)
				.distinct()
				.leftJoin(post.postTags, postTag).fetchJoin()
				.leftJoin(postTag.tag, tag).fetchJoin()
				.where(post.postId.in(postIds))
				.orderBy(post.postId.desc())
				.fetch();
	}

	@Override
	public List<Post> findPostByKeyword(String keyword, Long limit) {
		List<Long> postIds = queryFactory
				.select(post.postId)
				.from(post)
				.leftJoin(post.postTags, postTag)
				.leftJoin(postTag.tag, tag)
				.where(startsWithTitleOrTag(keyword))
				.orderBy(post.postId.desc())
				.distinct()
				.limit(limit+1)
				.fetch();
		return queryFactory
				.selectFrom(post)
				.distinct()
				.leftJoin(post.postTags, postTag).fetchJoin()
				.leftJoin(postTag.tag, tag).fetchJoin()
				.where(post.postId.in(postIds))
				.orderBy(post.postId.desc())
				.fetch();
	}

	@Override
	public List<Post> findPostByKeyword(String keyword, Long lastPostId, Long limit) {
		List<Long> postIds = queryFactory
				.select(post.postId)
				.from(post)
				.leftJoin(post.postTags, postTag)
				.leftJoin(postTag.tag, tag)
				.where(
					startsWithTitleOrTag(keyword),
					ltPostId(lastPostId)
				)
				.orderBy(post.postId.desc())
				.distinct()
				.limit(limit+1)
				.fetch();
		return queryFactory
				.selectFrom(post)
				.distinct()
				.leftJoin(post.postTags, postTag).fetchJoin()
				.leftJoin(postTag.tag, tag).fetchJoin()
				.where(post.postId.in(postIds))
				.orderBy(post.postId.desc())
				.fetch();
	}

	private BooleanExpression ltPostId(Long lastPostId) {
		return lastPostId != null ? post.postId.lt(lastPostId) : null;
	}

	private BooleanExpression inTagNames(List<String> tags) {
		return tags != null && !tags.isEmpty() ? tag.name.in(tags) : null;
	}

	private BooleanExpression startsWithTitleOrTag(String keyword) {
		if (keyword == null) {
			return null;
		}
		return post.title.startsWith(keyword)
				.or(tag.name.startsWith(keyword));
	}
}
