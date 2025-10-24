package collector.adapter.extractor.thumbnail

import collector.adapter.fetcher.Fetcher
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ThumbnailDownloader(
    private val fetcher: Fetcher
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun download(url: String): String? {

        val html = fetcher.fetch(url)

        val document = Jsoup.parse(html)

        // head 내부의 og:image 메타 태그 찾기
        val ogImage = document.select("meta[property=og:image]").attr("content")

        return ogImage.ifBlank { null }
    }
}