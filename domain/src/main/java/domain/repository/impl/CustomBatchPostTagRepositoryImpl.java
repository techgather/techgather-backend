package domain.repository.impl;

import application.generator.SnowFlake;
import domain.repository.CustomBatchPostTagRepository;
import domain.vo.PostTagPair;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomBatchPostTagRepositoryImpl implements CustomBatchPostTagRepository {

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

		List<PostTagPair> pairs = createPostTagList(urls, tagNames);
		List<PostTagPair> sanitizePairs = pairs.stream()
			    .distinct()
				.toList();
		saveBatch(sanitizePairs);
	}

	private List<PostTagPair> createPostTagList(List<String> urls, List<String> tagNames) {
		List<PostTagPair> postTagPairs = new ArrayList<>(urls.size());
		for (int i = 0; i < urls.size(); i++) {
			postTagPairs.add(PostTagPair.of(urls.get(i), tagNames.get(i)));
		}
		return postTagPairs;
	}

	private void saveBatch(List<PostTagPair> pairs) {
		for (int i = 0; i < pairs.size(); i += BATCH_SIZE) {
			int endIndex = Math.min(i + BATCH_SIZE, pairs.size());
			List<PostTagPair> batch = pairs.subList(i, endIndex);

			SqlParameterSource[] batchArgs = new SqlParameterSource[batch.size()];
			for (int j = 0; j < batch.size(); j++) {
				PostTagPair postTagPair = batch.get(j);
				MapSqlParameterSource params = new MapSqlParameterSource();
				params.addValue("id", snowflake.nextId());
				params.addValue("url", postTagPair.url());
				params.addValue("tagName", postTagPair.tagName());
				batchArgs[j] = params;
			}

			namedParameterJdbcTemplate.batchUpdate(INSERT_SQL, batchArgs);
		}
	}
}

