package collector.adapter.deduplicator

import collector.engine.model.Message
import collector.engine.port.DeduplicatePort
import org.springframework.stereotype.Component

@Component
class Deduplicator: DeduplicatePort {
    override fun deduplicate(messages: List<Message>): List<Message> {
        return messages
    }
}