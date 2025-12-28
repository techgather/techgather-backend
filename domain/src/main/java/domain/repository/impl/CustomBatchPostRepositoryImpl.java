package domain.repository.impl;

import domain.entity.Post;
import domain.repository.CustomBatchPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class CustomBatchPostRepositoryImpl implements CustomBatchPostRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final int BATCH_SIZE = 100;
    private static final String BINDING_PARAMETER = "?";
    private static final String DELIMITER = ", ";
    private static final String INSERT_SQL =
            "INSERT INTO techgather.post (post_id, title, url, pub_date, thumbnail, source_site_name, language, status) " +
            "VALUES (:postId, :title, :url, :pubDate, :thumbnail, :sourceSiteName, :language, :status) " +
            "ON DUPLICATE KEY UPDATE " +
            "title = VALUES(title), " +
            "pub_date = VALUES(pub_date), " +
            "thumbnail = VALUES(thumbnail), " +
            "source_site_name = VALUES(source_site_name), " +
            "language = VALUES(language), " +
            "status = VALUES(status)";
    private static final String SELECT_SQL = "SELECT post_id, url FROM techgather.post WHERE url IN (%s)";

    @Override
    public void saveAllPost(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }

        List<String> urls = posts.stream()
                .map(Post::getUrl)
                .distinct()
                .toList();

        Map<String, Long> postIds = findPostIdsByUrls(urls);

        List<Post> results = posts.stream()
                .map(post -> {
                    Long id = postIds.get(post.getUrl());
                    if (id != null) {
                        return Post.create(id,
                                post.getTitle(),
                                post.getUrl(),
                                post.getPubDate(),
                                post.getThumbnail(),
                                post.getSourceSiteName(),
                                post.getLanguage().getCode());
                    }
                    return post;
                })
                .toList();

        namedParameterJdbcTemplate.batchUpdate(INSERT_SQL, results.stream()
                .map(post -> {
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("postId", post.getPostId());
                    paramMap.put("title", post.getTitle());
                    paramMap.put("url", post.getUrl());
                    paramMap.put("pubDate", post.getPubDate());
                    paramMap.put("thumbnail", post.getThumbnail());
                    paramMap.put("sourceSiteName", post.getSourceSiteName());
                    paramMap.put("language", post.getLanguage().name());
                    paramMap.put("status", post.getStatus().name());
                    return new MapSqlParameterSource(paramMap);
                })
                .toArray(SqlParameterSource[]::new));
    }

    private Map<String, Long> findPostIdsByUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Long> map = new HashMap<>();
        for (int i = 0; i < urls.size(); i += BATCH_SIZE) {
            List<String> batchUrls = extractBatch(urls, i);
            Map<String, Long> batchResult = findQueryPostIdsByUrls(batchUrls);
            map.putAll(batchResult);
        }
        return map;
    }

    private List<String> extractBatch(List<String> urls, int startIndex) {
        int endIndex = Math.min(startIndex + BATCH_SIZE, urls.size());
        return urls.subList(startIndex, endIndex);
    }

    private Map<String, Long> findQueryPostIdsByUrls(List<String> urls) {
        String sql = buildSelectSqlWithInClause(urls.size());

        return jdbcTemplate.query(
                sql,
                ps -> bindUrlParameters(ps, urls),
                rs -> {
                    Map<String, Long> result = new HashMap<>();
                    while (rs.next()) {
                        String url = rs.getString("url");
                        Long postId = rs.getLong("post_id");
                        result.put(url, postId);
                    }
                    return result;
                }
        );
    }

    private String buildSelectSqlWithInClause(int parameterCount) {
        String placeholders = String.join(DELIMITER, Collections.nCopies(parameterCount, BINDING_PARAMETER));
        return String.format(SELECT_SQL, placeholders);
    }

    private void bindUrlParameters(PreparedStatement ps, List<String> urls) throws SQLException {
        for (int i = 0; i < urls.size(); i++) {
            ps.setString(i + 1, urls.get(i));
        }
    }
}
