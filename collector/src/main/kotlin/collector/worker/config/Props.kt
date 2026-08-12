package collector.worker.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "target")
class TargetProperties(
    var actives: List<String>,
    var entries: Map<String, TargetProps> = emptyMap()
)

@ConfigurationProperties(prefix = "collector.run")
class CollectorRunProperties(
    var runOnStartup: Boolean = false,
    var scheduledEnabled: Boolean = true,
    var cron: String = "0 0 3 * * *",
    var zone: String = "Asia/Seoul",
)

@ConfigurationProperties(prefix = "collector.batch-job")
class BatchJobTriggerProperties(
    var enabled: Boolean = true,
    var postIngestUrl: String = "http://localhost:7500/internal/jobs/post-ingest",
    var existingPostUrlsUrl: String = "",
    var token: String = "",
    var timeoutMillis: Long = 60000,
    var lookupTimeoutMillis: Long = 10000,
) {
    fun resolvedExistingPostUrlsUrl(): String = existingPostUrlsUrl.ifBlank {
        postIngestUrl.substringBeforeLast('/') + "/existing-post-urls"
    }
}

data class TargetProps(
    var url: String = "",
    var siteUrl: String = "",
    var language: String = "KO",
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
