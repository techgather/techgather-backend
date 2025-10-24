package collector.adapter.crawler

import collector.adapter.fetcher.HttpFetcher
import collector.engine.port.dto.CrawlingResult
import collector.engine.command.CollectCommand
import collector.engine.port.Crawler
import org.springframework.stereotype.Component

@Component
class HtmlCrawler(
    private val httpFetcher: HttpFetcher,
): Crawler {
    override suspend fun crawl(collectCommand: CollectCommand): CrawlingResult {

        val html = httpFetcher.fetch(collectCommand.url)

        return CrawlingResult(html)
    }
}