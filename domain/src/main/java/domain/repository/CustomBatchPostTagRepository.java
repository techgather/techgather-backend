package domain.repository;

import java.util.List;

public interface CustomBatchPostTagRepository {

	void saveAllUrlAndTag(List<String> urls, List<String> tagNames);

}

