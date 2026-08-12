package batch.service;

import domain.repository.CustomBatchPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostLookupService {

    private final CustomBatchPostRepository postRepository;

    public Set<String> findExistingUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return Set.of();
        }

        List<String> normalizedUrls = urls.stream()
                .filter(url -> url != null && !url.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        if (normalizedUrls.isEmpty()) {
            return Set.of();
        }

        return new LinkedHashSet<>(postRepository.findExistingUrls(normalizedUrls));
    }
}
