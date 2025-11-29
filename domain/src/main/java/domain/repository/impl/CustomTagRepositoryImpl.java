package domain.repository.impl;

import domain.entity.Tag;
import domain.repository.CustomTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class CustomTagRepositoryImpl implements CustomTagRepository {

	private final JdbcTemplate jdbcTemplate;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private static final int BATCH_SIZE = 100;
	private static final String BINDING_PARAMETER = "?";
	private static final String DELIMITER = ", ";
	private static final String INSERT_SQL = "INSERT INTO techgather.tag (id, name) VALUES (:id, :name) " +
 											 "ON DUPLICATE KEY UPDATE " +
 											 "name = VALUES(name)";
	private static final String SELECT_SQL = "SELECT id, name FROM techgather.tag WHERE name IN (%s)";

	@Override
	public void saveAllTag(List<Tag> tags) {
		if (tags == null || tags.isEmpty()) {
			return;
		}

		List<String> names = tags.stream()
				.map(Tag::getName)
				.distinct()
				.toList();

		Map<String, Long> tagIds = findTagIdsByNames(names);

		List<Tag> tagList = tags.stream()
				.map(tag -> {
					Long id = tagIds.get(tag.getName());
					if (id != null) {
						return Tag.create(id,
								          tag.getName());
					}
					return tag;
				})
				.toList();

		namedParameterJdbcTemplate.batchUpdate(INSERT_SQL, tagList.stream()
				.map(tag -> {
					MapSqlParameterSource params = new MapSqlParameterSource();
					params.addValue("id", tag.getId());
					params.addValue("name", tag.getName());
					return params;
				})
				.toArray(SqlParameterSource[]::new));
	}

	private Map<String, Long> findTagIdsByNames(List<String> names) {
		if (names == null || names.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<String, Long> map = new HashMap<>();
		for (int i = 0; i < names.size(); i += BATCH_SIZE) {
			List<String> batchNames = extractBatch(names, i);
			Map<String, Long> batchResult = queryTagIdsByNames(batchNames);
			map.putAll(batchResult);
		}

		return map;
	}

	private List<String> extractBatch(List<String> names, int startIndex) {
		int endIndex = Math.min(startIndex + BATCH_SIZE, names.size());
		return names.subList(startIndex, endIndex);
	}

	private Map<String, Long> queryTagIdsByNames(List<String> names) {
		String sql = buildSelectSqlWithInClause(names.size());

		return jdbcTemplate.query(
			sql,
			ps -> bindNameParameters(ps, names),
			rs -> {
				Map<String, Long> result = new HashMap<>();
				while (rs.next()) {
					String name = rs.getString("name");
					Long id = rs.getLong("id");
					result.put(name, id);
				}
				return result;
			}
		);
	}

	private String buildSelectSqlWithInClause(int parameterCount) {
		String placeholders = String.join(DELIMITER, Collections.nCopies(parameterCount, BINDING_PARAMETER));
		return String.format(SELECT_SQL, placeholders);
	}

	private void bindNameParameters(java.sql.PreparedStatement ps, List<String> names) throws java.sql.SQLException {
		for (int i = 0; i < names.size(); i++) {
			ps.setString(i + 1, names.get(i));
		}
	}
}

