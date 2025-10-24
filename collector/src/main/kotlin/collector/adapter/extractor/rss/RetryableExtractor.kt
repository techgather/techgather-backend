package collector.adapter.extractor.rss

import collector.engine.command.ExtractCommand
import collector.engine.model.Message
import collector.engine.port.Extractor
import collector.engine.port.dto.CrawlingResult
import collector.utils.retry.retrySuspend
import org.slf4j.LoggerFactory

abstract class RetryableExtractor: Extractor {

    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun extract(crawlingResult: CrawlingResult, extractCommand: ExtractCommand): List<Message> {

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
    ): List<Message>

    private fun recoverForExtract(): List<Message> {

        return listOf()
    }


}