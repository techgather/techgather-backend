package collector.adapter.extractor

import collector.engine.command.ExtractCommand
import collector.engine.port.dto.CrawlingResult
import collector.engine.model.Message
import collector.engine.port.Extractor
import org.springframework.stereotype.Component

@Component
class DefaultExtractor: Extractor {
    override suspend fun extract(crawlingResult: CrawlingResult, extractCommand: ExtractCommand): List<Message> {
        TODO("Not yet implemented")
    }
}