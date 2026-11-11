package api.service

import com.fasterxml.jackson.databind.ObjectMapper
import domain.entity.Category
import domain.entity.Post
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import org.springframework.web.client.RestClient
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OpenAiPostClassifierTest {
    private val mapper = ObjectMapper()
    private val category = Category.create(2L, null, "백엔드", "backend", "백엔드 기술")
    private val categories = mapOf("backend" to category)
    private val post = Post.create(1L, "Spring Batch", "https://example.com/post",
        LocalDateTime.now(), null, "example", "KO")
    private lateinit var classifier: OpenAiPostClassifier
    private lateinit var server: MockRestServiceServer

    @BeforeEach
    fun setUp() {
        classifier = OpenAiPostClassifier(mapper, true, "https://api.openai.com/v1",
            "test-key", "gpt-5.4-mini", "none", 30000, 0, 40)
        val builder = RestClient.builder().baseUrl("https://api.openai.com/v1")
        server = MockRestServiceServer.bindTo(builder).build()
        ReflectionTestUtils.setField(classifier, "restClient", builder.build())
    }

    @Test
    fun `OpenAI 요청 형식으로 호출하고 카테고리를 반환한다`() {
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
            .andRespond(withSuccess(response("backend"), MediaType.APPLICATION_JSON))

        assertEquals(category, classifier.classify(post, categories).orElseThrow())
        server.verify()
    }

    @Test
    fun `분류 불가 응답이면 키워드 분류에 사용할 빈 결과를 반환한다`() {
        server.expect(anything()).andRespond(withSuccess(response(null), MediaType.APPLICATION_JSON))
        assertTrue(classifier.classify(post, categories).isEmpty)
        server.verify()
    }

    @Test
    fun `호출 한도에 도달하면 이번 실행에서는 추가 요청을 보내지 않는다`() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS))
        assertTrue(classifier.classify(post, categories).isEmpty)
        assertTrue(classifier.classify(post, categories).isEmpty)
        server.verify()

        server.reset()
        classifier.resetRunState()
        server.expect(anything()).andRespond(withSuccess(response("backend"), MediaType.APPLICATION_JSON))
        assertEquals(category, classifier.classify(post, categories).orElseThrow())
        server.verify()
    }

    @Test
    fun `API 키가 없으면 호출하지 않는다`() {
        val unavailable = OpenAiPostClassifier(mapper, true, "https://api.openai.com/v1",
            "", "gpt-5.4-mini", "none", 30000, 0, 40)
        assertFalse(unavailable.isAvailable)
        assertTrue(unavailable.classify(post, categories).isEmpty)
    }

    private fun response(slug: String?): String {
        val content = mapper.writeValueAsString(mapOf("slug" to slug, "confidence" to 0.9))
        return mapper.writeValueAsString(mapOf("choices" to listOf(mapOf("message" to mapOf("content" to content)))))
    }
}
