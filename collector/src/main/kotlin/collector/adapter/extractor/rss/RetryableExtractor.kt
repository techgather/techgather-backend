package collector.adapter.extractor.rss

import collector.engine.command.ExtractCommand
import collector.engine.model.ExtractedMessage
import collector.engine.port.Extractor
import collector.engine.port.dto.CrawlingResult
import collector.utils.retry.retrySuspend
import org.slf4j.LoggerFactory

abstract class RetryableExtractor: Extractor {

    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun extract(crawlingResult: CrawlingResult, extractCommand: ExtractCommand): List<ExtractedMessage> {

        return retrySuspend(
            maxAttempts = 3,
            initialDelay = 1000,
            maxDelay = 4000,
            multiplier = 2.0,
            block = { doExtract(crawlingResult, extractCommand) },
            recover = { recoverForExtract() }
        )
    }

    protected abstract suspend fun doExtract(
        crawlingResult: CrawlingResult,
        extractCommand: ExtractCommand
    ): List<ExtractedMessage>

    private fun recoverForExtract(): List<ExtractedMessage> {

        return listOf()
    }


}