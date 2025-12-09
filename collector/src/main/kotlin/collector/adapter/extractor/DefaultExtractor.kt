package collector.adapter.extractor

import collector.engine.command.ExtractCommand
import collector.engine.model.ExtractedMessage
import collector.engine.port.dto.CrawlingResult
import collector.engine.port.Extractor
import org.springframework.stereotype.Component

@Component
class DefaultExtractor: Extractor {
    override suspend fun extract(crawlingResult: CrawlingResult, extractCommand: ExtractCommand): List<ExtractedMessage> {
        TODO("Not yet implemented")
    }
}