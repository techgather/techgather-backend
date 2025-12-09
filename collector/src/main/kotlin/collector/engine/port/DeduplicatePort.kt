package collector.engine.port

import collector.engine.model.ExtractedMessage

interface DeduplicatePort {

    fun deduplicate(messages: List<ExtractedMessage>): List<ExtractedMessage>
}