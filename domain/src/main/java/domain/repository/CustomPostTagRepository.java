package domain.repository;

import java.util.List;

public interface CustomPostTagRepository {

	void saveAllUrlAndTag(List<String> urls, List<String> tagNames);

}

