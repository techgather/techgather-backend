package domain.repository.impl;

import application.generator.SnowFlake;
import domain.repository.CustomPostTagRepository;
import domain.vo.PostTag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomPostTagRepositoryImpl implements CustomPostTagRepository {

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private final SnowFlake snowflake = SnowFlake.getInstance();

	private static final int BATCH_SIZE = 100;
	private static final String INSERT_SQL = "INSERT IGNORE INTO techgather.post_tag (id, post_id, tag_id) " +
											 "SELECT :id, p.post_id, t.id " +
 											 "FROM (SELECT :url AS url) AS input " +
 											 "INNER JOIN techgather.post p ON p.url = input.url " +
											 "INNER JOIN techgather.tag t ON t.name = :tagName";

	@Override
	public void saveAllUrlAndTag(List<String> urls, List<String> tagNames) {
		if (urls == null || urls.isEmpty()) {
			return;
		}

		if (tagNames == null || tagNames.isEmpty()) {
			return;
		}

		List<PostTag> pairs = createPostTagList(urls, tagNames);
		List<PostTag> sanitizePairs = pairs.stream()
			    .distinct()
				.toList();
		saveBatch(sanitizePairs);
	}

	private List<PostTag> createPostTagList(List<String> urls, List<String> tagNames) {
		List<PostTag> postTags = new ArrayList<>(urls.size());
		for (int i = 0; i < urls.size(); i++) {
			postTags.add(PostTag.of(urls.get(i), tagNames.get(i)));
		}
		return postTags;
	}

	private void saveBatch(List<PostTag> pairs) {
		for (int i = 0; i < pairs.size(); i += BATCH_SIZE) {
			int endIndex = Math.min(i + BATCH_SIZE, pairs.size());
			List<PostTag> batch = pairs.subList(i, endIndex);

			SqlParameterSource[] batchArgs = new SqlParameterSource[batch.size()];
			for (int j = 0; j < batch.size(); j++) {
				PostTag postTag = batch.get(j);
				MapSqlParameterSource params = new MapSqlParameterSource();
				params.addValue("id", snowflake.nextId());
				params.addValue("url", postTag.url());
				params.addValue("tagName", postTag.tagName());
				batchArgs[j] = params;
			}

			namedParameterJdbcTemplate.batchUpdate(INSERT_SQL, batchArgs);
		}
	}
}

