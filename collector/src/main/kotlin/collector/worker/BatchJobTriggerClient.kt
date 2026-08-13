package collector.worker

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import collector.worker.config.BatchJobTriggerProperties
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.header
import io.ktor.client.request.post
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BatchJobTriggerClient(
    private val properties: BatchJobTriggerProperties,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = properties.timeoutMillis
            connectTimeoutMillis = properties.timeoutMillis
            socketTimeoutMillis = properties.timeoutMillis
        }
    }

    suspend fun triggerPostIngest(): PostIngestTriggerResult {
        if (!properties.enabled) {
            log.info("Batch post-ingest trigger skipped. enabled=false")
            return PostIngestTriggerResult(
                statusCode = null,
                completed = false,
                skipped = true,
                summary = null,
                errorMessage = null
            )
        }

        return try {
            val response = client.post(properties.postIngestUrl) {
                if (properties.token.isNotBlank()) {
                    header(INTERNAL_JOB_TOKEN_HEADER, properties.token)
                }
            }

            val responseBody = response.bodyAsText()
            val completed = response.status.value in 200..299
            val summary = runCatching {
                objectMapper.readValue(responseBody, PostIngestSummary::class.java)
            }.onFailure { e ->
                log.error(
                    "Batch post-ingest response parsing failed. status={}, body={}",
                    response.status.value,
                    responseBody,
                    e
                )
            }.getOrNull()

            if (!completed) {
                log.warn("Batch post-ingest trigger failed. status={}, body={}", response.status.value, responseBody)
            } else {
                log.info("Batch post-ingest trigger completed. status={}, body={}", response.status.value, responseBody)
            }

            PostIngestTriggerResult(
                statusCode = response.status.value,
                completed = completed,
                skipped = false,
                summary = summary,
                errorMessage = null
            )
        } catch (e: Exception) {
            log.error("Batch post-ingest trigger failed", e)
            PostIngestTriggerResult(
                statusCode = null,
                completed = false,
                skipped = false,
                summary = null,
                errorMessage = e.message ?: e::class.simpleName
            )
        }
    }

    @PreDestroy
    fun close() {
        client.close()
    }

    private companion object {
        private const val INTERNAL_JOB_TOKEN_HEADER = "X-Internal-Job-Token"
    }

    data class PostIngestTriggerResult(
        val statusCode: Int?,
        val completed: Boolean,
        val skipped: Boolean,
        val summary: PostIngestSummary?,
        val errorMessage: String?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PostIngestSummary(
        val jobStatus: String? = null,
        val steps: List<PostIngestStepSummary> = emptyList(),
        val failureMessages: List<String> = emptyList(),
        val uniquePostCount: Long? = null,
        val insertedPostCount: Long? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PostIngestStepSummary(
        val readCount: Long = 0,
        val writeCount: Long = 0
    )
}
