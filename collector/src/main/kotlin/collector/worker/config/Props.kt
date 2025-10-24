package collector.worker.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "target")
class TargetProperties(
    var actives: List<String>,
    var entries: Map<String, TargetProps> = emptyMap()
)

data class TargetProps(
    var url: String = "",
    var adapter: AdapterProps,
)

data class AdapterProps(
    var crawler: CrawlerProps,
    var extractor: ExtractorProps,
)

data class CrawlerProps(
    var type: String,
)

data class ExtractorProps(
    var type: String,
    var useDefaultThumbnail: Boolean = false,
    var defaultThumbnail: String? = null
)