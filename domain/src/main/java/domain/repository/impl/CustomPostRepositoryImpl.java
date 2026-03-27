package domain.repository.impl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import domain.constants.Language;
import domain.constants.PostStatus;
import domain.entity.Post;
import domain.repository.CustomPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
    public List<Post> searchPosts(Language language, String keyword, List<String> categorySlugs, List<String> sourceSiteNames, PostStatus status, Long limit) {
        List<Long> postIds = findPostIds(language, keyword, categorySlugs, sourceSiteNames, status, null, null, limit);
        return findPostsWithTagsById(postIds);
    }

    @Override
    public List<Post> searchPosts(Language language, String keyword, List<String> categorySlugs, List<String> sourceSiteNames, PostStatus status, LocalDateTime lastPubDate, Long lastPostId, Long limit) {
        List<Long> postIds = findPostIds(language, keyword, categorySlugs, sourceSiteNames, status, lastPubDate, lastPostId, limit);
        return findPostsWithTagsById(postIds);
    }

    @Override
    public long countPosts(Language language, String keyword, List<String> categorySlugs, List<String> sourceSiteNames, PostStatus status) {
        JPAQuery<Long> query = queryFactory
                .select(post.postId.countDistinct())
                .from(post);

        if (keyword != null || (categorySlugs != null && !categorySlugs.isEmpty())) {
            query.leftJoin(post.postCategories, postCategory)
                    .leftJoin(postCategory.category, category);
        }

        Long count = query
                .where(
                        hasStatus(status),
                        hasLanguage(language),
                        hasSourceSiteNames(sourceSiteNames),
                        matchesKeyword(keyword),
                        hasCategories(categorySlugs)
                )
                .fetchOne();

        return count == null ? 0L : count;
    }

    private List<Long> findPostIds(Language language, String keyword, List<String> categorySlugs, List<String> sourceSiteNames, PostStatus status, LocalDateTime cursorPubDate, Long cursorPostId, Long limit) {
        JPAQuery<Tuple> query = queryFactory
                .select(post.postId, post.pubDate)
                .from(post);

        if (keyword != null || (categorySlugs != null && !categorySlugs.isEmpty())) {
            query.leftJoin(post.postCategories, postCategory)
                    .leftJoin(postCategory.category, category);
        }

        return query
                .where(
                        hasStatus(status),
                        hasLanguage(language),
                        hasSourceSiteNames(sourceSiteNames),
                        matchesKeyword(keyword),
                        hasCategories(categorySlugs),
                        afterCursor(cursorPubDate, cursorPostId)
                )
                .orderBy(post.pubDate.desc(), post.postId.desc())
                .distinct()
                .limit(limit)
                .fetch()
                .stream()
                .map(tuple -> tuple.get(post.postId))
                .toList();
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
                .orderBy(post.pubDate.desc(), post.postId.desc())
                .fetch();
    }

    private BooleanExpression hasStatus(PostStatus status) {
        return status != null ? post.status.eq(status) : null;
    }

    private BooleanExpression afterCursor(LocalDateTime cursorPubDate, Long cursorPostId) {
        if (cursorPubDate == null || cursorPostId == null) {
            return null;
        }
        return post.pubDate.lt(cursorPubDate)
                .or(post.pubDate.eq(cursorPubDate).and(post.postId.lt(cursorPostId)));
    }

    private BooleanExpression matchesKeyword(String keyword) {
        return keyword != null
                ? post.title.containsIgnoreCase(keyword).or(category.name.containsIgnoreCase(keyword))
                : null;
    }

    private BooleanExpression hasLanguage(Language language) {
        return language != null ? post.language.eq(language) : null;
    }

    private BooleanExpression hasSourceSiteNames(List<String> sourceSiteNames) {
        if (sourceSiteNames == null || sourceSiteNames.isEmpty()) {
            return null;
        }
        return post.sourceSiteName.lower().in(sourceSiteNames);
    }

    private BooleanExpression hasCategories(List<String> categorySlugs) {
        if (categorySlugs == null || categorySlugs.isEmpty()) {
            return null;
        }
        return category.slug.in(categorySlugs);
    }
}
