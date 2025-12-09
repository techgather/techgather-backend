package collector.adapter.deduplicator

import collector.engine.model.ExtractedMessage
import collector.engine.model.Message
import collector.engine.port.DeduplicatePort
import org.springframework.stereotype.Component

@Component
class Deduplicator: DeduplicatePort {
    override fun deduplicate(messages: List<ExtractedMessage>): List<ExtractedMessage> {
        return messages
    }
}