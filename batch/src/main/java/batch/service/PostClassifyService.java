// 수집된 게시글에 카테고리를 자동으로 분류하는 서비스
package batch.service;

import application.generator.SnowFlake;
import domain.entity.Category;
import domain.entity.Post;
import domain.entity.PostCategory;
import domain.repository.CategoryRepository;
import domain.repository.PostCategoryRepository;
import domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostClassifyService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final SnowFlake snowFlake = SnowFlake.getInstance();

    // slug → 매칭 키워드 목록 (소문자, 순서대로 우선순위 높음)
    private static final List<Map.Entry<String, List<String>>> KEYWORD_RULES = List.of(
        Map.entry("agent", List.of(
            "agent", "에이전트", "mcp", "agentic", "tool use", "tool calling", "function calling",
            "autonomous", "multi-agent", "multiagent"
        )),
        Map.entry("ai", List.of(
            "llm", "gpt", "chatgpt", "claude", "gemini", "인공지능", "머신러닝", "딥러닝",
            "machine learning", "deep learning", "자연어처리", "nlp", "생성형", "파인튜닝",
            "fine-tuning", "finetuning", "rag", "embedding", "transformer", "bert",
            "diffusion", "stable diffusion", "openai", "anthropic", "ai 모델", "언어 모델",
            "대규모 언어", "추론", "벡터 검색", "vector db", "vector store"
        )),
        Map.entry("data", List.of(
            "데이터 엔지니어", "data engineer", "spark", "airflow", "kafka", "etl",
            "데이터 파이프라인", "data pipeline", "데이터 플랫폼", "data platform",
            "bigquery", "redshift", "snowflake", "dbt", "데이터 레이크", "data lake",
            "데이터 웨어하우스", "data warehouse", "bi ", "tableau", "superset",
            "flink", "beam", "batch processing", "stream processing", "데이터 분석"
        )),
        Map.entry("infra", List.of(
            "kubernetes", "k8s", "쿠버네티스", "docker", "컨테이너", "container",
            "terraform", "인프라", "infrastructure", "aws", "gcp", "azure", "클라우드",
            "cloud", "devops", "ci/cd", "cicd", "배포", "deployment", "서버리스",
            "serverless", "helm", "argocd", "istio", "nginx", "grafana", "prometheus",
            "모니터링", "monitoring", "observability", "sre ", "site reliability"
        )),
        Map.entry("security", List.of(
            "보안", "security", "취약점", "vulnerability", "인증", "인가", "oauth",
            "jwt", "암호화", "encryption", "xss", "sql injection", "csrf", "해킹",
            "hacking", "cve", "penetration", "침투", "제로트러스트", "zero trust",
            "sso", "saml", "2fa", "mfa"
        )),
        Map.entry("qa-test", List.of(
            "테스트", "test", "qa ", "quality assurance", "tdd", "bdd", "단위 테스트",
            "unit test", "통합 테스트", "integration test", "e2e", "selenium",
            "playwright", "cypress", "junit", "testcontainer", "자동화 테스트",
            "코드 품질", "code quality", "정적 분석", "sonar"
        )),
        Map.entry("mobile", List.of(
            "ios", "android", "swift", "flutter", "react native",
            "모바일", "mobile", "앱 개발", "app develop", "swiftui", "jetpack compose"
        )),
        Map.entry("web", List.of(
            "react", "vue", "angular", "next.js", "nuxt", "svelte",
            "프론트엔드", "frontend", "front-end", "웹 프론트", "javascript", "typescript",
            "css", "html", "webpack", "vite", "웹 성능", "web performance", "렌더링"
        )),
        Map.entry("design", List.of(
            "ux", "ui ", "디자인", "design", "사용자 경험", "user experience",
            "figma", "디자인 시스템", "design system", "접근성", "accessibility",
            "인터랙션", "interaction", "프로토타입", "prototype"
        )),
        Map.entry("culture", List.of(
            "개발 문화", "조직 문화", "팀 문화", "tech culture", "성장", "회고",
            "retrospective", "온보딩", "onboarding", "채용", "hiring", "협업",
            "collaboration", "애자일", "agile", "스크럼", "scrum", "개발자 문화",
            "엔지니어링 문화"
        )),
        Map.entry("backend", List.of(
            "spring", "django", "fastapi", "express", "nestjs", "백엔드", "backend",
            "back-end", "api 서버", "rest api", "graphql", "grpc", "msa",
            "마이크로서비스", "microservice", "jvm", "java ", "kotlin ", "golang",
            "go ", "rust ", "서버 개발", "서버 성능", "데이터베이스", "database",
            "mysql", "postgresql", "redis", "mongodb", "orm", "jpa"
        ))
    );

    @Transactional
    public void classifyUnclassifiedPosts() {
        List<Post> posts = postRepository.findPublishedUnclassifiedPosts();
        if (posts.isEmpty()) {
            log.info("[분류] 미분류 게시글 없음");
            return;
        }

        log.info("[분류] 미분류 게시글 {}개 분류 시작", posts.size());

        Map<String, Category> categoryBySlug = categoryRepository.findAllByOrderByNameAsc()
                .stream()
                .collect(Collectors.toMap(Category::getSlug, c -> c));

        int classified = 0;
        for (Post post : posts) {
            Optional<Category> category = resolveCategory(post, categoryBySlug);
            if (category.isEmpty()) {
                continue;
            }
            postCategoryRepository.save(PostCategory.create(snowFlake.nextId(), post, category.get()));
            classified++;
        }

        log.info("[분류] {}개 분류 완료 (미분류 유지: {}개)", classified, posts.size() - classified);
    }

    private Optional<Category> resolveCategory(Post post, Map<String, Category> categoryBySlug) {
        String text = buildSearchText(post);

        for (Map.Entry<String, List<String>> rule : KEYWORD_RULES) {
            String slug = rule.getKey();
            List<String> keywords = rule.getValue();

            if (!categoryBySlug.containsKey(slug)) {
                continue;
            }

            for (String keyword : keywords) {
                if (text.contains(keyword)) {
                    return Optional.of(categoryBySlug.get(slug));
                }
            }
        }

        return Optional.empty();
    }

    private String buildSearchText(Post post) {
        StringBuilder sb = new StringBuilder();
        if (post.getTitle() != null) {
            sb.append(post.getTitle().toLowerCase());
            sb.append(" ");
        }
        Set<domain.entity.PostTag> postTags = post.getPostTags();
        if (postTags != null && !postTags.isEmpty()) {
            postTags.forEach(pt -> {
                if (pt.getTag() != null && pt.getTag().getName() != null) {
                    sb.append(pt.getTag().getName().toLowerCase());
                    sb.append(" ");
                }
            });
        }
        return sb.toString();
    }
}
