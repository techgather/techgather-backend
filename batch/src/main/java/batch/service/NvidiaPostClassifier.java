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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NvidiaPostClassifier {

    private final boolean enabled;
    private final String apiKey;
    private final String model;
    private final String reasoningEffort;
    private final long minIntervalMillis;
    private final int maxCallsPerRun;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private long lastRequestAtMillis = 0L;
    private int callsThisRun = 0;
    private boolean disabledForRun = false;

    private static final Pattern SLUG_PATTERN = Pattern.compile("\"slug\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern CONFIDENCE_PATTERN = Pattern.compile("\"confidence\"\\s*:\\s*([0-9.]+)");

    public NvidiaPostClassifier(
            ObjectMapper objectMapper,
            @Value("${classification.llm.enabled:false}") boolean enabled,
            @Value("${classification.llm.base-url:https://integrate.api.nvidia.com/v1}") String baseUrl,
            @Value("${classification.llm.api-key:}") String apiKey,
            @Value("${classification.llm.model:deepseek-ai/deepseek-v4-flash}") String model,
            @Value("${classification.llm.reasoning-effort:none}") String reasoningEffort,
            @Value("${classification.llm.timeout-millis:30000}") long timeoutMillis,
            @Value("${classification.llm.min-interval-millis:1500}") long minIntervalMillis,
            @Value("${classification.llm.max-calls-per-run:40}") int maxCallsPerRun
    ) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.model = model;
        this.reasoningEffort = reasoningEffort;
        this.minIntervalMillis = minIntervalMillis;
        this.maxCallsPerRun = maxCallsPerRun;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(timeoutMillis));
        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMillis));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public synchronized void resetRunState() {
        callsThisRun = 0;
        disabledForRun = false;
    }

    public boolean isAvailable() {
        return enabled && StringUtils.hasText(apiKey);
    }

    public Optional<Category> classify(Post post, Map<String, Category> categoryBySlug) {
        if (!isAvailable() || !reserveCall()) {
            return Optional.empty();
        }

        try {
            waitForRateLimit();
            ChatCompletionResponse response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(buildRequest(post, categoryBySlug))
                    .retrieve()
                    .body(ChatCompletionResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                return Optional.empty();
            }

            String content = response.choices().get(0).message().content();
            ClassificationResult result = parseClassificationResult(content);
            if (result == null || !StringUtils.hasText(result.slug())) {
                return Optional.empty();
            }

            if ("null".equalsIgnoreCase(result.slug())) {
                return Optional.empty();
            }

            Category category = categoryBySlug.get(result.slug());
            if (category == null) {
                log.warn("[분류] NVIDIA LLM이 존재하지 않는 카테고리를 반환했습니다. postId={}, slug={}", post.getPostId(), result.slug());
                return Optional.empty();
            }

            log.info("[분류] NVIDIA LLM 분류 성공. postId={}, slug={}, confidence={}", post.getPostId(), result.slug(), result.confidence());
            return Optional.of(category);
        } catch (HttpStatusCodeException e) {
            int statusCode = e.getStatusCode().value();
            if (statusCode == 429 || statusCode == 503) {
                disableForCurrentRun("[분류] NVIDIA LLM 호출 한도 도달. 이번 분류 실행에서는 키워드 fallback만 사용합니다. status=%d, body=%s"
                        .formatted(statusCode, abbreviate(e.getResponseBodyAsString())));
                return Optional.empty();
            }
            log.warn("[분류] NVIDIA LLM 호출 실패. postId={}, status={}, body={}",
                    post.getPostId(), statusCode, abbreviate(e.getResponseBodyAsString()));
            return Optional.empty();
        } catch (Exception e) {
            log.warn("[분류] NVIDIA LLM 분류 실패. postId={}", post.getPostId(), e);
            return Optional.empty();
        }
    }

    private synchronized boolean reserveCall() {
        if (disabledForRun) {
            return false;
        }
        if (maxCallsPerRun > 0 && callsThisRun >= maxCallsPerRun) {
            disabledForRun = true;
            log.info("[분류] NVIDIA LLM 실행당 호출 제한 도달. maxCallsPerRun={}", maxCallsPerRun);
            return false;
        }
        callsThisRun++;
        return true;
    }

    private synchronized void disableForCurrentRun(String message) {
        if (!disabledForRun) {
            log.warn(message);
        }
        disabledForRun = true;
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

    private ChatCompletionRequest buildRequest(Post post, Map<String, Category> categoryBySlug) {
        String categories = categoryBySlug.values().stream()
                .map(category -> "- " + category.getSlug() + ": " + category.getName() + " / " + category.getDescription())
                .collect(Collectors.joining("\n"));

        String userPrompt = """
                다음 기술 블로그 게시글을 가장 적합한 카테고리 slug 하나로 분류하세요.
                반드시 available_categories에 있는 slug 중 하나만 선택하세요.
                확신이 낮으면 {"slug":null,"confidence":0.0} 형식으로 답하세요.
                첫 글자는 반드시 { 이어야 하고 마지막 글자는 반드시 } 이어야 합니다.
                설명, 마크다운, 코드블록 없이 한 줄 JSON만 답하세요.

                available_categories:
                %s

                post:
                title: %s
                sourceSiteName: %s
                tags: %s
                """.formatted(
                categories,
                nullToEmpty(post.getTitle()),
                nullToEmpty(post.getSourceSiteName()),
                buildTagText(post)
        );

        return new ChatCompletionRequest(
                model,
                List.of(
                        new Message("system", "Return exactly one compact JSON object and no other text. Schema: {\"slug\":\"category-slug-or-null\",\"confidence\":0.0}."),
                        new Message("user", userPrompt)
                ),
                0.0,
                0.7,
                128,
                reasoningEffort,
                false
        );
    }

    private ClassificationResult parseClassificationResult(String content) throws Exception {
        if (!StringUtils.hasText(content)) {
            return null;
        }
        String cleaned = content.trim()
                .replaceFirst("^```json\\s*", "")
                .replaceFirst("^```\\s*", "")
                .replaceFirst("\\s*```$", "");
        String json = extractJsonObject(cleaned);

        try {
            return objectMapper.readValue(json, ClassificationResult.class);
        } catch (Exception e) {
            ClassificationResult result = parseSlugLeniently(cleaned);
            if (result != null) {
                log.warn("[분류] NVIDIA LLM 응답이 엄격한 JSON이 아니어서 slug만 추출했습니다. response={}", abbreviate(cleaned));
                return result;
            }
            log.warn("[분류] NVIDIA LLM 응답 JSON 파싱 실패. response={}", abbreviate(cleaned));
            return null;
        }
    }

    private String extractJsonObject(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }

    private ClassificationResult parseSlugLeniently(String content) {
        Matcher slugMatcher = SLUG_PATTERN.matcher(content);
        if (!slugMatcher.find()) {
            return null;
        }

        Double confidence = null;
        Matcher confidenceMatcher = CONFIDENCE_PATTERN.matcher(content);
        if (confidenceMatcher.find()) {
            confidence = Double.parseDouble(confidenceMatcher.group(1));
        }
        return new ClassificationResult(slugMatcher.group(1), confidence);
    }

    private String abbreviate(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 300) {
            return normalized;
        }
        return normalized.substring(0, 300) + "...";
    }

    private String buildTagText(Post post) {
        Set<domain.entity.PostTag> postTags = post.getPostTags();
        if (postTags == null || postTags.isEmpty()) {
            return "";
        }
        return postTags.stream()
                .filter(postTag -> postTag.getTag() != null && postTag.getTag().getName() != null)
                .map(postTag -> postTag.getTag().getName())
                .collect(Collectors.joining(", "));
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
