package collector.worker.provider

import collector.engine.port.Crawler
import collector.engine.port.Extractor
import org.springframework.stereotype.Component

@Component
class AdapterProvider(
    private val crawlers: Map<String, Crawler>,
    private val extractors: Map<String, Extractor>,
) {

    fun getCrawler(key: String): Crawler {
        return crawlers[key] ?: crawlers["default"] ?: throw IllegalArgumentException("Crawler not found: $key")
    }

    fun getExtractor(key: String): Extractor {
        return extractors[key] ?: extractors["default"] ?: throw IllegalArgumentException("Extractor not found: $key")
    }

}