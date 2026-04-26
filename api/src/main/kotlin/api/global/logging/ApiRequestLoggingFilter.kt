package api.global.logging

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingResponseWrapper
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@Component
class ApiRequestLoggingFilter(
    private val objectMapper: ObjectMapper,
    @Value("\${logging.api.app-name:CareNote-BE}")
    private val appName: String,
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger("api.request")
    private val zoneId = ZoneId.of("Asia/Seoul")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val startedAt = System.currentTimeMillis()
        val wrappedResponse = ContentCachingResponseWrapper(response)
        val requestId = request.getHeader(REQUEST_ID_HEADER)?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()

        try {
            filterChain.doFilter(request, wrappedResponse)
        } finally {
            writeApiLog(request, wrappedResponse, startedAt, requestId)
            wrappedResponse.copyBodyToResponse()
        }
    }

    private fun writeApiLog(
        request: HttpServletRequest,
        response: ContentCachingResponseWrapper,
        startedAt: Long,
        requestId: String,
    ) {
        val status = response.status
        val code = extractErrorCode(response) ?: defaultCode(status)
        val payload = linkedMapOf(
            "timestamp" to OffsetDateTime.now(zoneId).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            "app" to appName,
            "method" to request.method,
            "path" to request.requestURI,
            "status" to status.toString(),
            "code" to code,
            "accountId" to "-",
            "profileId" to "-",
            "durationMs" to (System.currentTimeMillis() - startedAt).toString(),
            "requestId" to requestId,
            "ip" to clientIp(request),
        )

        log.info(objectMapper.writeValueAsString(payload))
    }

    private fun extractErrorCode(response: ContentCachingResponseWrapper): String? {
        if (response.status < 400 || response.contentAsByteArray.isEmpty()) {
            return null
        }

        return runCatching {
            objectMapper.readTree(response.contentAsByteArray).path("code").asText()
                .takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    private fun defaultCode(status: Int): String {
        return when (status) {
            in 200..399 -> "-"
            400 -> "E400000"
            401 -> "E401101"
            403 -> "E403000"
            404 -> "E404000"
            in 500..599 -> "E500000"
            else -> "-"
        }
    }

    private fun clientIp(request: HttpServletRequest): String {
        return request.getHeader("X-Forwarded-For")
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: request.getHeader("X-Real-IP")?.takeIf { it.isNotBlank() }
            ?: request.remoteAddr
    }

    companion object {
        private const val REQUEST_ID_HEADER = "X-Request-Id"
    }
}
