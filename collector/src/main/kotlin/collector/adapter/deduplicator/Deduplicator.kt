package collector.adapter.deduplicator

import collector.engine.model.ExtractedMessage
import collector.engine.port.DeduplicatePort
import collector.engine.port.ExistingPostLookupPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class Deduplicator(
    private val existingPostLookupPort: ExistingPostLookupPort,
): DeduplicatePort {
    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun deduplicate(messages: List<ExtractedMessage>): List<ExtractedMessage> {
        val uniqueMessages = messages
            .mapNotNull { message ->
                message.url.trim()
                    .takeIf(String::isNotBlank)
                    ?.let { normalizedUrl -> message.copy(url = normalizedUrl) }
            }
            .distinctBy { it.url }

        if (uniqueMessages.isEmpty()) {
            return emptyList()
        }

        return try {
            val existingUrls = existingPostLookupPort.findExistingUrls(uniqueMessages.map { it.url })
            val newMessages = uniqueMessages.filterNot { it.url in existingUrls }
            log.info(
                "Post deduplication completed. extracted={}, unique={}, existing={}, publishing={}",
                messages.size,
                uniqueMessages.size,
                uniqueMessages.size - newMessages.size,
                newMessages.size
            )
            newMessages
        } catch (e: Exception) {
            log.warn(
                "Existing post lookup failed; publishing {} unique messages without persisted deduplication",
                uniqueMessages.size,
                e
            )
            uniqueMessages
        }
    }
}
