package collector.engine.port

import collector.engine.model.Message

interface DeduplicatePort {

    fun deduplicate(messages: List<Message>): List<Message>
}