package collector.adapter.extractor.bucketplace

import collector.adapter.extractor.thumbnail.ThumbnailDownloader
import collector.engine.command.ExtractCommand
import collector.engine.model.ExtractedMessage
import collector.engine.port.Extractor
import collector.engine.port.dto.CrawlingResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.net.URI
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Component("bucketplaceExtractor")
class BucketplaceExtractor(
    private val thumbnailDownloader: ThumbnailDownloader
) : Extractor {

    override suspend fun extract(
        crawlingResult: CrawlingResult,
        extractCommand: ExtractCommand
    ): List<ExtractedMessage> = coroutineScope {
        val document = Jsoup.parse(crawlingResult.body)

        document.select("a[href]")
            .mapNotNull { link -> toArticle(link, extractCommand) }
            .distinctBy { it.url }
            .map { article ->
                async {
                    article.copy(thumbnail = getThumbnail(article.url, extractCommand))
                }
            }
            .awaitAll()
    }

    private fun toArticle(link: Element, extractCommand: ExtractCommand): ExtractedMessage? {
        val href = link.attr("href")
        val articlePath = runCatching { URI.create(href).path }.getOrNull() ?: href
        if (!ARTICLE_PATH.matches(articlePath)) {
            return null
        }

        val url = link.absUrl("href").ifBlank { "https://www.bucketplace.com$href" }
        val title = link.select("h2, h3, h4").firstOrNull()?.text()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: link.attr("aria-label").trim().takeIf { it.isNotBlank() }
            ?: link.text().trim().takeIf { it.isNotBlank() }
            ?: return null

        val description = link.select("p").firstOrNull()?.text()?.trim()
        val pubDate = link.select("time[datetime]").firstOrNull()
            ?.attr("datetime")
            ?.let { parseDate(it) }
            ?: LocalDateTime.now()

        return ExtractedMessage(
            title = title,
            url = url,
            pubDate = pubDate,
            tags = listOf("Design"),
            description = description,
            thumbnail = extractCommand.defaultThumbnail
        )
    }

    private fun parseDate(value: String): LocalDateTime? {
        return runCatching { LocalDateTime.parse(value) }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(value).toLocalDateTime() }.getOrNull()
    }

    private suspend fun getThumbnail(url: String, command: ExtractCommand): String? {
        if (command.useDefaultThumbnail) return command.defaultThumbnail

        return try {
            thumbnailDownloader.download(url) ?: command.defaultThumbnail
        } catch (e: Exception) {
            command.defaultThumbnail
        }
    }

    companion object {
        private val ARTICLE_PATH = Regex("^/culture/\\d+/?$")
    }
}
