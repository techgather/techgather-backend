package collector.engine

import collector.engine.port.Publisher
import collector.engine.port.Extractor
import collector.engine.command.CollectCommand
import collector.engine.port.Crawler
import collector.engine.port.DeduplicatePort
import org.slf4j.LoggerFactory

class CollectEngine(
    private val crawler: Crawler,
    private val extractor: Extractor,
    private val deduplicatePort: DeduplicatePort,
    private val publisher: Publisher,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun run(command: CollectCommand) {

        val startTime = System.currentTimeMillis()

        // 크롤링 후 message 추출
        val crawlingResult = crawler.crawl(command)

        //messages 추출
        val messages = try {
            extractor.extract(crawlingResult, command.extractCommand)
        } catch (e: Exception) {
            log.error("Failed to extract messages from ${command.name}", e)
            return
        }

        //중복 제거
        val deduplicatedMessages = deduplicatePort.deduplicate(messages)

        //메시지 publish
        publisher.publish(deduplicatedMessages)

        log.info("Published ${deduplicatedMessages.size} messages from ${command.name} (${(System.currentTimeMillis() - startTime).toDouble() / 1000}s)")
    }
}