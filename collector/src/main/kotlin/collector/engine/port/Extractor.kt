package collector.engine.port

import collector.engine.command.ExtractCommand
import collector.engine.model.Message
import collector.engine.port.dto.CrawlingResult

interface Extractor {
    suspend fun extract(crawlingResult: CrawlingResult, extractCommand: ExtractCommand): List<Message>
}