package collector.adapter.extractor.rss

import collector.adapter.extractor.thumbnail.ThumbnailDownloader
import collector.engine.command.ExtractCommand
import collector.engine.model.ExtractedMessage
import collector.engine.port.dto.CrawlingResult
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
open class RssExtractor(
    private val thumbnailDownloader: ThumbnailDownloader,
): RetryableExtractor() {

    private val xmlMapper = XmlMapper().findAndRegisterModules()
    private val illegalXmlCharsRegex = Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]")

    override suspend fun doExtract(crawlingResult: CrawlingResult, extractCommand: ExtractCommand): List<ExtractedMessage> = coroutineScope {

        val sanitizedBody = illegalXmlCharsRegex.replace(crawlingResult.body, "")

        val rss = xmlMapper.readValue(sanitizedBody, Rss::class.java)
        
        return@coroutineScope rss.channel.items?.map { item ->
            async {
                ExtractedMessage(
                    title = item.title,
                    url = item.link,
                    pubDate = item.pubDate,
                    tags = item.categories ?: listOf(),
                    description = item.description,
                    thumbnail = getThumbnail(item.link, extractCommand)
                )
            }
        }?.awaitAll() ?: emptyList()
    }

    private suspend fun getThumbnail(url: String, command: ExtractCommand): String? {

        if(command.useDefaultThumbnail) return command.defaultThumbnail

        return thumbnailDownloader.download(url) ?: command.defaultThumbnail
    }
}