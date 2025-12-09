package collector.engine.port

import collector.engine.command.ExtractCommand
import collector.engine.model.ExtractedMessage
import collector.engine.port.dto.CrawlingResult

interface Extractor {
    suspend fun extract(crawlingResult: CrawlingResult, extractCommand: ExtractCommand): List<ExtractedMessage>
}