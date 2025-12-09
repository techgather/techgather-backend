package collector.engine.model

import java.time.LocalDateTime

data class ExtractedMessage (
    val title: String,
    val url: String,
    val pubDate: LocalDateTime,
    val tags: List<String>,
    val description: String?,
    val thumbnail: String?,
)