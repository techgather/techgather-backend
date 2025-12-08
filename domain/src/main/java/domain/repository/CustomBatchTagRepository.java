package domain.repository;

import domain.entity.Tag;

import java.util.List;

public interface CustomBatchTagRepository {

	void saveAllTag(List<Tag> tags);
}

