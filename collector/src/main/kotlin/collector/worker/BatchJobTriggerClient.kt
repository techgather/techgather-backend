package collector.worker

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
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = properties.timeoutMillis
            connectTimeoutMillis = properties.timeoutMillis
            socketTimeoutMillis = properties.timeoutMillis
        }
    }

    suspend fun triggerPostIngest() {
        if (!properties.enabled) {
            log.info("Batch post-ingest trigger skipped. enabled=false")
            return
        }

        val response = client.post(properties.postIngestUrl) {
            if (properties.token.isNotBlank()) {
                header(INTERNAL_JOB_TOKEN_HEADER, properties.token)
            }
        }

        val responseBody = response.bodyAsText()
        if (response.status.value !in 200..299) {
            error("Batch post-ingest trigger failed. status=${response.status.value}, body=$responseBody")
        }

        log.info("Batch post-ingest trigger completed. status={}, body={}", response.status.value, responseBody)
    }

    @PreDestroy
    fun close() {
        client.close()
    }

    private companion object {
        private const val INTERNAL_JOB_TOKEN_HEADER = "X-Internal-Job-Token"
    }
}
