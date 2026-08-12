package batch.service;

import domain.repository.CustomBatchPostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostLookupServiceTest {

    @Mock
    private CustomBatchPostRepository postRepository;

    @Test
    void normalizesUrlsAndReturnsExistingUrls() {
        PostLookupService service = new PostLookupService(postRepository);
        ArgumentCaptor<List<String>> urlsCaptor = ArgumentCaptor.forClass(List.class);
        when(postRepository.findExistingUrls(List.of("https://example.com/1", "https://example.com/2")))
                .thenReturn(List.of("https://example.com/2"));

        Set<String> result = service.findExistingUrls(List.of(
                " https://example.com/1 ",
                "https://example.com/1",
                "https://example.com/2",
                " "
        ));

        verify(postRepository).findExistingUrls(urlsCaptor.capture());
        assertThat(urlsCaptor.getValue())
                .containsExactly("https://example.com/1", "https://example.com/2");
        assertThat(result).containsExactly("https://example.com/2");
    }

    @Test
    void skipsRepositoryForEmptyUrls() {
        PostLookupService service = new PostLookupService(postRepository);

        assertThat(service.findExistingUrls(List.of())).isEmpty();

        verify(postRepository, never()).findExistingUrls(List.of());
    }
}
