package api.service

import api.service.dto.result.ClassifyPostsResult
import application.generator.SnowFlake
import domain.constants.PostStatus
import domain.entity.Category
import domain.entity.Post
import domain.entity.PostCategory
import domain.repository.CategoryRepository
import domain.repository.PostCategoryRepository
import domain.repository.PostRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class PostAutoClassifyService(
    private val postRepository: PostRepository,
    private val categoryRepository: CategoryRepository,
    private val postCategoryRepository: PostCategoryRepository,
    private val nvidiaPostClassifier: NvidiaPostClassifier
) {

    private val snowFlake = SnowFlake.getInstance()

    fun normalizePostIds(postIds: List<String>): List<String> {
        return parseIds(postIds).map(Long::toString)
    }

    @Transactional
    fun classifyPosts(postIds: List<String>): ClassifyPostsResult {
        val parsedPostIds = parseIds(postIds)
        val posts = postRepository.findAllById(parsedPostIds)
        val postById = posts.associateBy { it.postId }
        val missingPostIds = parsedPostIds.filterNot(postById::containsKey)

        val categoryBySlug = categoryRepository.findAllByOrderByNameAsc()
            .associateBy { it.slug }
        if (categoryBySlug.isEmpty()) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "분류 가능한 카테고리가 없습니다.")
        }

        val classifiedPostIds = mutableListOf<Long>()
        val llmClassifiedPostIds = mutableListOf<Long>()
        val keywordClassifiedPostIds = mutableListOf<Long>()
        val onHoldPostIds = mutableListOf<Long>()
        nvidiaPostClassifier.resetRunState()

        postById.values.forEach { post ->
            val llmCategory = nvidiaPostClassifier.classify(post, categoryBySlug)
            val category = if (llmCategory.isPresent) llmCategory.get() else resolveCategory(post, categoryBySlug)
            if (category == null) {
                onHoldPostIds.add(post.postId)
                return@forEach
            }

            if (!postCategoryRepository.existsByPostPostIdAndCategoryId(post.postId, category.id)) {
                postCategoryRepository.save(PostCategory.create(snowFlake.nextId(), post, category))
            }
            classifiedPostIds.add(post.postId)
            if (llmCategory.isPresent) {
                llmClassifiedPostIds.add(post.postId)
            } else {
                keywordClassifiedPostIds.add(post.postId)
            }
        }

        if (onHoldPostIds.isNotEmpty()) {
            postRepository.updateStatusByPostId(onHoldPostIds, PostStatus.ON_HOLD)
        }

        return ClassifyPostsResult(
            requested = parsedPostIds.size,
            found = postById.size,
            classifiedPostIds = classifiedPostIds,
            llmClassifiedPostIds = llmClassifiedPostIds,
            keywordClassifiedPostIds = keywordClassifiedPostIds,
            onHoldPostIds = onHoldPostIds,
            missingPostIds = missingPostIds
        )
    }

    private fun resolveCategory(post: Post, categoryBySlug: Map<String, Category>): Category? {
        val text = buildSearchText(post)
        for ((slug, keywords) in KEYWORD_RULES) {
            val category = categoryBySlug[slug] ?: continue
            if (keywords.any { keyword -> text.contains(keyword) }) {
                return category
            }
        }
        return null
    }

    private fun buildSearchText(post: Post): String {
        return buildString {
            post.title?.let {
                append(it.lowercase())
                append(" ")
            }
            post.postTags?.forEach { postTag ->
                postTag.tag?.name?.let {
                    append(it.lowercase())
                    append(" ")
                }
            }
        }
    }

    private fun parseIds(ids: List<String>): List<Long> {
        val parsedIds = ids.map { rawId ->
            val normalizedId = rawId.trim()
            if (normalizedId.isEmpty()) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "postIds에 빈 값이 포함되어 있습니다.")
            }
            normalizedId.toLongOrNull()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid postId: $normalizedId")
        }.distinct()

        if (parsedIds.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "postIds는 비어 있을 수 없습니다.")
        }
        return parsedIds
    }

    private companion object {
        private val KEYWORD_RULES = listOf(
            "agent" to listOf(
                "agent", "에이전트", "mcp", "agentic", "tool use", "tool calling", "function calling",
                "autonomous", "multi-agent", "multiagent"
            ),
            "ai" to listOf(
                "llm", "gpt", "chatgpt", "claude", "gemini", "인공지능", "머신러닝", "딥러닝",
                "machine learning", "deep learning", "자연어처리", "nlp", "생성형", "파인튜닝",
                "fine-tuning", "finetuning", "rag", "embedding", "transformer", "bert",
                "diffusion", "stable diffusion", "openai", "anthropic", "ai 모델", "언어 모델",
                "대규모 언어", "추론", "벡터 검색", "vector db", "vector store"
            ),
            "data" to listOf(
                "데이터 엔지니어", "data engineer", "spark", "airflow", "kafka", "etl",
                "데이터 파이프라인", "data pipeline", "데이터 플랫폼", "data platform",
                "bigquery", "redshift", "snowflake", "dbt", "데이터 레이크", "data lake",
                "데이터 웨어하우스", "data warehouse", "bi ", "tableau", "superset",
                "flink", "beam", "batch processing", "stream processing", "데이터 분석"
            ),
            "infra" to listOf(
                "kubernetes", "k8s", "쿠버네티스", "docker", "컨테이너", "container",
                "terraform", "인프라", "infrastructure", "aws", "gcp", "azure", "클라우드",
                "cloud", "devops", "ci/cd", "cicd", "배포", "deployment", "서버리스",
                "serverless", "helm", "argocd", "istio", "nginx", "grafana", "prometheus",
                "모니터링", "monitoring", "observability", "sre ", "site reliability"
            ),
            "security" to listOf(
                "보안", "security", "취약점", "vulnerability", "인증", "인가", "oauth",
                "jwt", "암호화", "encryption", "xss", "sql injection", "csrf", "해킹",
                "hacking", "cve", "penetration", "침투", "제로트러스트", "zero trust",
                "sso", "saml", "2fa", "mfa"
            ),
            "qa-test" to listOf(
                "테스트", "test", "qa ", "quality assurance", "tdd", "bdd", "단위 테스트",
                "unit test", "통합 테스트", "integration test", "e2e", "selenium",
                "playwright", "cypress", "junit", "testcontainer", "자동화 테스트",
                "코드 품질", "code quality", "정적 분석", "sonar"
            ),
            "mobile" to listOf(
                "ios", "android", "swift", "flutter", "react native",
                "모바일", "mobile", "앱 개발", "app develop", "swiftui", "jetpack compose"
            ),
            "web" to listOf(
                "react", "vue", "angular", "next.js", "nuxt", "svelte",
                "프론트엔드", "frontend", "front-end", "웹 프론트", "javascript", "typescript",
                "css", "html", "webpack", "vite", "웹 성능", "web performance", "렌더링"
            ),
            "design" to listOf(
                "ux", "ui ", "디자인", "design", "사용자 경험", "user experience",
                "figma", "디자인 시스템", "design system", "접근성", "accessibility",
                "인터랙션", "interaction", "프로토타입", "prototype"
            ),
            "culture" to listOf(
                "개발 문화", "조직 문화", "팀 문화", "tech culture", "성장", "회고",
                "retrospective", "온보딩", "onboarding", "협업",
                "collaboration", "애자일", "agile", "스크럼", "scrum", "개발자 문화",
                "엔지니어링 문화"
            ),
            "backend" to listOf(
                "spring", "django", "fastapi", "express", "nestjs", "백엔드", "backend",
                "back-end", "api 서버", "rest api", "graphql", "grpc", "msa",
                "마이크로서비스", "microservice", "jvm", "java ", "kotlin ", "golang",
                "go ", "rust ", "서버 개발", "서버 성능", "데이터베이스", "database",
                "mysql", "postgresql", "redis", "mongodb", "orm", "jpa"
            )
        )
    }
}
