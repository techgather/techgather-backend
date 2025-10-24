package collector.engine.port

import collector.engine.model.Message

interface Publisher {

    fun publish(messages: List<Message>)
}