package collector.engine.port

import collector.engine.model.ExtractedMessage

interface DeduplicatePort {

    suspend fun deduplicate(messages: List<ExtractedMessage>): List<ExtractedMessage>
}
