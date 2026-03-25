package domain.repository.impl;

import domain.entity.Tag;
import domain.repository.CustomBatchTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CustomBatchTagRepositoryImpl implements CustomBatchTagRepository {

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private static final int BATCH_SIZE = 100;
	private static final int MAX_RETRY = 3;
	private static final long RETRY_BACKOFF_MS = 30L;
	private static final String INSERT_SQL = "INSERT IGNORE INTO tag (id, name) VALUES (:id, :name)";

	@Override
	public void saveAllTag(List<Tag> tags) {
		if (tags == null || tags.isEmpty()) {
			return;
		}

		List<Tag> sortedDistinctTags = tags.stream()
				.filter(tag -> tag.getName() != null)
				.sorted(Comparator.comparing(Tag::getName))
				.collect(
						LinkedHashMap<String, Tag>::new,
						(map, tag) -> map.putIfAbsent(tag.getName(), tag),
						Map::putAll
				)
				.values()
				.stream()
				.toList();

		for (int i = 0; i < sortedDistinctTags.size(); i += BATCH_SIZE) {
			int endIndex = Math.min(i + BATCH_SIZE, sortedDistinctTags.size());
			List<Tag> batch = sortedDistinctTags.subList(i, endIndex);
			executeBatchWithRetry(batch);
		}
	}

	private void executeBatchWithRetry(List<Tag> batch) {
		int attempt = 0;
		while (true) {
			try {
				namedParameterJdbcTemplate.batchUpdate(INSERT_SQL, toSqlParameterSources(batch));
				return;
			} catch (PessimisticLockingFailureException e) {
				attempt++;
				if (attempt >= MAX_RETRY) {
					throw e;
				}
				sleepBeforeRetry(attempt);
			}
		}
	}

	private SqlParameterSource[] toSqlParameterSources(List<Tag> batch) {
		List<SqlParameterSource> params = new ArrayList<>(batch.size());
		for (Tag tag : batch) {
			MapSqlParameterSource source = new MapSqlParameterSource();
			source.addValue("id", tag.getId());
			source.addValue("name", tag.getName());
			params.add(source);
		}
		return params.toArray(SqlParameterSource[]::new);
	}

	private void sleepBeforeRetry(int attempt) {
		try {
			Thread.sleep(RETRY_BACKOFF_MS * attempt);
		} catch (InterruptedException interruptedException) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while retrying tag batch insert", interruptedException);
		}
	}
}
