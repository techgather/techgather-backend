package domain.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import domain.constants.Language;
import domain.constants.PostStatus;
import domain.entity.Post;
import domain.repository.CustomPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static domain.entity.QPost.post;
import static domain.entity.QPostCategory.postCategory;
import static domain.entity.QPostTag.postTag;
import static domain.entity.QCategory.category;
import static domain.entity.QCategoryGroup.categoryGroup;
import static domain.entity.QTag.tag;

@Repository
@RequiredArgsConstructor
public class CustomPostRepositoryImpl implements CustomPostRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> searchPosts(Language language, String keyword, List<Long> categoryIds, String sourceSiteName, PostStatus status, Long limit) {
        List<Long> postIds = findPostIds(language, keyword, categoryIds, sourceSiteName, status, null, limit);
        return findPostsWithTagsById(postIds);
    }

    @Override
    public List<Post> searchPosts(Language language, String keyword, List<Long> categoryIds, String sourceSiteName, PostStatus status, Long lastPostId, Long limit) {
        List<Long> postIds = findPostIds(language, keyword, categoryIds, sourceSiteName, status, lastPostId, limit);
        return findPostsWithTagsById(postIds);
    }

    private List<Long> findPostIds(Language language, String keyword, List<Long> categoryIds, String sourceSiteName, PostStatus status, Long cursorPostId, Long limit) {
        JPAQuery<Long> query = queryFactory
                .select(post.postId)
                .from(post);

        if (keyword != null) {
            query.leftJoin(post.postTags, postTag)
                    .leftJoin(postTag.tag, tag);
        }
        if (categoryIds != null && !categoryIds.isEmpty()) {
            query.leftJoin(post.postCategories, postCategory)
                    .leftJoin(postCategory.category, category);
        }

        return query
                .where(
                    hasStatus(status),
                    hasLanguage(language),
                    hasSourceSiteName(sourceSiteName),
                    matchesKeyword(keyword),
                    hasCategories(categoryIds),
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
                .leftJoin(post.postCategories, postCategory).fetchJoin()
                .leftJoin(postCategory.category, category).fetchJoin()
                .leftJoin(category.categoryGroup, categoryGroup).fetchJoin()
                .where(post.postId.in(postIds))
                .orderBy(post.postId.desc())
                .fetch();
    }

    private BooleanExpression hasStatus(PostStatus status) {
        return status != null ? post.status.eq(status) : null;
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

    private BooleanExpression hasSourceSiteName(String sourceSiteName) {
        if (sourceSiteName == null || sourceSiteName.isBlank()) {
            return null;
        }
        return post.sourceSiteName.equalsIgnoreCase(sourceSiteName.trim());
    }

    private BooleanExpression hasCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return null;
        }
        return category.id.in(categoryIds);
    }
}
