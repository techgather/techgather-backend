package batch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import domain.entity.Category;
import domain.entity.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class OpenAiPostClassifierTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Category category = Category.create(2L, null, "백엔드", "backend", "백엔드 기술");
    private final Post post = Post.create(1L, "Spring Batch", "https://example.com/post",
            LocalDateTime.now(), null, "example", "KO");
    private OpenAiPostClassifier classifier;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        classifier = new OpenAiPostClassifier(mapper, true, "https://api.openai.com/v1",
                "test-key", "gpt-5.4-mini", "none", 30000, 0);
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        ReflectionTestUtils.setField(classifier, "restClient", builder.build());
    }

    @Test
    void sendsOpenAiRequestAndResolvesCategory() throws Exception {
        server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-key"))
                .andExpect(jsonPath("$.model").value("gpt-5.4-mini"))
                .andExpect(jsonPath("$.max_completion_tokens").value(512))
                .andExpect(jsonPath("$.reasoning_effort").value("none"))
                .andExpect(jsonPath("$.response_format.type").value("json_object"))
                .andExpect(jsonPath("$.max_tokens").doesNotExist())
                .andExpect(jsonPath("$.temperature").doesNotExist())
                .andExpect(jsonPath("$.top_p").doesNotExist())
                .andRespond(withSuccess(response("backend"), MediaType.APPLICATION_JSON));

        assertEquals(category, classifier.classify(post, List.of("spring"), Map.of("backend", category)).orElseThrow());
        server.verify();
    }

    @Test
    void returnsEmptyForUnclassifiedResponse() throws Exception {
        server.expect(anything()).andRespond(withSuccess(response(null), MediaType.APPLICATION_JSON));
        assertTrue(classifier.classify(post, List.of(), Map.of("backend", category)).isEmpty());
        server.verify();
    }

    @Test
    void propagatesRateLimitForRetryInsteadOfHoldingPost() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        assertThrows(PostClassificationException.class,
                () -> classifier.classify(post, List.of(), Map.of("backend", category)));
        server.verify();
    }

    @Test
    void skipsWhenApiKeyIsMissing() {
        OpenAiPostClassifier unavailable = new OpenAiPostClassifier(mapper, true,
                "https://api.openai.com/v1", "", "gpt-5.4-mini", "none", 30000, 0);
        assertFalse(unavailable.isAvailable());
        assertTrue(unavailable.classify(post, List.of(), Map.of("backend", category)).isEmpty());
    }

    private String response(String slug) throws Exception {
        String content = mapper.writeValueAsString(new Classification(slug, 0.9));
        return mapper.writeValueAsString(Map.of("choices", List.of(Map.of("message", Map.of("content", content)))));
    }

    private record Classification(String slug, double confidence) {}
}
