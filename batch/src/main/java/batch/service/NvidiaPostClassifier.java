package batch.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.entity.Category;
import domain.entity.Post;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NvidiaPostClassifier {

    private static final Pattern SLUG_PATTERN = Pattern.compile("\"slug\"\\s*:\\s*\"([^\"]+)\"");

    private final boolean enabled;
    private final String apiKey;
    private final String model;
    private final String reasoningEffort;
    private final long minIntervalMillis;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private long lastRequestAtMillis;

    public NvidiaPostClassifier(
            ObjectMapper objectMapper,
            @Value("${classification.llm.enabled:false}") boolean enabled,
            @Value("${classification.llm.base-url:https://integrate.api.nvidia.com/v1}") String baseUrl,
            @Value("${classification.llm.api-key:}") String apiKey,
            @Value("${classification.llm.model:deepseek-ai/deepseek-v4-flash}") String model,
            @Value("${classification.llm.reasoning-effort:none}") String reasoningEffort,
            @Value("${classification.llm.timeout-millis:30000}") long timeoutMillis,
            @Value("${classification.llm.min-interval-millis:1500}") long minIntervalMillis
    ) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.model = model;
        this.reasoningEffort = reasoningEffort;
        this.minIntervalMillis = minIntervalMillis;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(timeoutMillis));
        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMillis));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public Optional<Category> classify(
            Post post,
            String description,
            Collection<String> tags,
            Map<String, Category> categoryBySlug
    ) {
        if (!enabled || !StringUtils.hasText(apiKey) || categoryBySlug.isEmpty()) {
            return Optional.empty();
        }

        try {
            waitForRateLimit();
            ChatCompletionResponse response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(buildRequest(post, description, tags, categoryBySlug))
                    .retrieve()
                    .body(ChatCompletionResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                return Optional.empty();
            }

            ClassificationResult result = parseResult(response.choices().get(0).message().content());
            if (result == null || !StringUtils.hasText(result.slug()) || "null".equalsIgnoreCase(result.slug())) {
                return Optional.empty();
            }

            Category category = categoryBySlug.get(result.slug());
            if (category == null) {
                log.warn("[적재 분류] NVIDIA LLM이 존재하지 않는 카테고리를 반환했습니다. postId={}, slug={}",
                        post.getPostId(), result.slug());
                return Optional.empty();
            }

            log.info("[적재 분류] NVIDIA LLM 분류 성공. postId={}, slug={}, confidence={}",
                    post.getPostId(), result.slug(), result.confidence());
            return Optional.of(category);
        } catch (HttpStatusCodeException e) {
            log.warn("[적재 분류] NVIDIA LLM 호출 실패. postId={}, status={}",
                    post.getPostId(), e.getStatusCode().value());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("[적재 분류] NVIDIA LLM 분류 실패. postId={}", post.getPostId(), e);
            return Optional.empty();
        }
    }

    private synchronized void waitForRateLimit() {
        long now = System.currentTimeMillis();
        long nextAllowedAt = lastRequestAtMillis + minIntervalMillis;
        if (now < nextAllowedAt) {
            try {
                Thread.sleep(nextAllowedAt - now);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for NVIDIA LLM rate limit", e);
            }
        }
        lastRequestAtMillis = System.currentTimeMillis();
    }

    private ChatCompletionRequest buildRequest(
            Post post,
            String description,
            Collection<String> tags,
            Map<String, Category> categoryBySlug
    ) {
        String categories = categoryBySlug.values().stream()
                .map(category -> "- " + category.getSlug() + ": " + category.getName() + " / " + category.getDescription())
                .collect(Collectors.joining("\n"));
        String tagText = tags == null ? "" : tags.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(", "));
        String prompt = """
                다음 기술 블로그 게시글을 가장 적합한 카테고리 slug 하나로 분류하세요.
                반드시 available_categories에 있는 slug 중 하나만 선택하세요.
                확신이 낮으면 {"slug":null,"confidence":0.0} 형식으로 답하세요.
                설명, 마크다운, 코드블록 없이 한 줄 JSON만 답하세요.

                available_categories:
                %s

                post:
                title: %s
                description: %s
                sourceSiteName: %s
                tags: %s
                """.formatted(categories, nullToEmpty(post.getTitle()), nullToEmpty(description),
                nullToEmpty(post.getSourceSiteName()), tagText);

        return new ChatCompletionRequest(
                model,
                List.of(
                        new Message("system", "Return exactly one compact JSON object and no other text. Schema: {\"slug\":\"category-slug-or-null\",\"confidence\":0.0}."),
                        new Message("user", prompt)
                ),
                0.0,
                0.7,
                128,
                reasoningEffort,
                false
        );
    }

    private ClassificationResult parseResult(String content) {
        if (!StringUtils.hasText(content)) {
            return null;
        }
        String cleaned = content.trim()
                .replaceFirst("^```json\\s*", "")
                .replaceFirst("^```\\s*", "")
                .replaceFirst("\\s*```$", "");
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        String json = start >= 0 && end > start ? cleaned.substring(start, end + 1) : cleaned;

        try {
            return objectMapper.readValue(json, ClassificationResult.class);
        } catch (Exception ignored) {
            Matcher matcher = SLUG_PATTERN.matcher(cleaned);
            return matcher.find() ? new ClassificationResult(matcher.group(1), null) : null;
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private record ChatCompletionRequest(
            String model,
            List<Message> messages,
            double temperature,
            @JsonProperty("top_p") double topP,
            @JsonProperty("max_tokens") int maxTokens,
            @JsonProperty("reasoning_effort") String reasoningEffort,
            boolean stream
    ) {
    }

    private record Message(String role, String content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatCompletionResponse(List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(MessageResponse message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record MessageResponse(String content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ClassificationResult(String slug, Double confidence) {
    }
}
