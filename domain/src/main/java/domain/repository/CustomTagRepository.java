package domain.repository;

import domain.entity.Tag;

import java.util.List;

public interface CustomTagRepository {

	void saveAllTag(List<Tag> tags);
}

