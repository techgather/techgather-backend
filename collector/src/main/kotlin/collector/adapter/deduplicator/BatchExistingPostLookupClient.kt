package collector.adapter.deduplicator

import com.fasterxml.jackson.databind.ObjectMapper
import collector.engine.port.ExistingPostLookupPort
import collector.worker.config.BatchJobTriggerProperties
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Component

@Component
class BatchExistingPostLookupClient(
    private val properties: BatchJobTriggerProperties,
    private val objectMapper: ObjectMapper,
) : ExistingPostLookupPort {
    private val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = properties.lookupTimeoutMillis
            connectTimeoutMillis = properties.lookupTimeoutMillis
            socketTimeoutMillis = properties.lookupTimeoutMillis
        }
    }

    override suspend fun findExistingUrls(urls: List<String>): Set<String> {
        if (!properties.enabled || urls.isEmpty()) {
            return emptySet()
        }

        val response = client.post(properties.resolvedExistingPostUrlsUrl()) {
            contentType(ContentType.Application.Json)
            if (properties.token.isNotBlank()) {
                header(INTERNAL_JOB_TOKEN_HEADER, properties.token)
            }
            setBody(objectMapper.writeValueAsString(ExistingPostUrlsRequest(urls)))
        }

        if (response.status.value !in 200..299) {
            throw IllegalStateException("Existing post lookup failed: HTTP ${response.status.value}")
        }

        return objectMapper
            .readValue(response.bodyAsText(), ExistingPostUrlsResponse::class.java)
            .existingUrls
    }

    @PreDestroy
    fun close() {
        client.close()
    }

    private data class ExistingPostUrlsRequest(val urls: List<String>)
    private data class ExistingPostUrlsResponse(val existingUrls: Set<String> = emptySet())

    private companion object {
        private const val INTERNAL_JOB_TOKEN_HEADER = "X-Internal-Job-Token"
    }
}
