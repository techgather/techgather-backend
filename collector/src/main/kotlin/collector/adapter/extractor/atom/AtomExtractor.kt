package collector.adapter.extractor.atom

import collector.adapter.extractor.rss.RetryableExtractor
import collector.adapter.extractor.thumbnail.ThumbnailDownloader
import collector.engine.command.ExtractCommand
import collector.engine.model.ExtractedMessage
import collector.engine.port.dto.CrawlingResult
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component("atomExtractor")
class AtomExtractor(
    private val thumbnailDownloader: ThumbnailDownloader,
) : RetryableExtractor() {

    private val xmlMapper = XmlMapper().findAndRegisterModules()
    private val illegalXmlCharsRegex = Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]")

    override suspend fun doExtract(crawlingResult: CrawlingResult, extractCommand: ExtractCommand): List<ExtractedMessage> = coroutineScope {

        val sanitizedBody = illegalXmlCharsRegex.replace(crawlingResult.body, "")
        return@coroutineScope try {
            val atom = xmlMapper.readValue(sanitizedBody, Atom::class.java)
            val entries = atom.entries ?: emptyList()

            entries.mapNotNull { entry ->
                val title = entry.title ?: return@mapNotNull null
                val link = entry.links?.firstOrNull { it.href != null }?.href ?: return@mapNotNull null

                val description = entry.summary ?: entry.content
                val pubDate = entry.updated ?: LocalDateTime.now()

                async {
                    ExtractedMessage(
                        title = title,
                        url = link,
                        pubDate = pubDate,
                        tags = emptyList(),
                        description = description,
                        thumbnail = getThumbnail(link, extractCommand)
                    )
                }
            }.awaitAll()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun getThumbnail(url: String, command: ExtractCommand): String? {
        if (command.useDefaultThumbnail) return command.defaultThumbnail

        return try {
            thumbnailDownloader.download(url) ?: command.defaultThumbnail
        } catch (e: Exception) {
            command.defaultThumbnail
        }
    }
}


