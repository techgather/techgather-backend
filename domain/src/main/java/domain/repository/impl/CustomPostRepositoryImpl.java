package domain.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import domain.constants.Language;
import domain.entity.Post;
import domain.constants.Status;
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
    public List<Post> searchPosts(Language language, String keyword, Long limit) {
        List<Long> postIds = findPostIds(language, keyword, null, limit);
        return findPostsWithTagsById(postIds);
    }

    @Override
    public List<Post> searchPosts(Language language, String keyword, Long lastPostId, Long limit) {
        List<Long> postIds = findPostIds(language, keyword, lastPostId, limit);
        return findPostsWithTagsById(postIds);
    }

    private List<Long> findPostIds(Language language, String keyword, Long cursorPostId, Long limit) {
        JPAQuery<Long> query = queryFactory
                .select(post.postId)
                .from(post);

        if (keyword != null) {
            query.leftJoin(post.postTags, postTag)
                    .leftJoin(postTag.tag, tag);
        }

        return query
                .where(
                    isPublished(),
                    hasLanguage(language),
                    matchesKeyword(keyword),
                    afterCursor(cursorPostId)
                )
                .orderBy(post.postId.desc())
                .distinct()
                .limit(limit)
                .fetch();
    }

    private List<Post> findPostsWithTagsById(List<Long> postIds) {
        return queryFactory
                .selectFrom(post)
                .distinct()
                .leftJoin(post.postTags, postTag).fetchJoin()
                .leftJoin(postTag.tag, tag).fetchJoin()
                .where(post.postId.in(postIds))
                .orderBy(post.postId.desc())
                .fetch();
    }

    private BooleanExpression isPublished() {
        return post.status.eq(Status.PUBLISHED);
    }

    private BooleanExpression afterCursor(Long cursorPostId) {
        return cursorPostId != null ? post.postId.lt(cursorPostId) : null;
    }

    private BooleanExpression matchesKeyword(String keyword) {
        return keyword != null
                ? post.title.startsWith(keyword).or(tag.name.startsWith(keyword))
                : null;
    }

    private BooleanExpression hasLanguage(Language language) {
        return language != null ? post.language.eq(language) : null;
    }
}
