package collector.engine.command

data class ExtractCommand (
    val useDefaultThumbnail: Boolean,
    val defaultThumbnail: String?,
)