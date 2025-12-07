package collector.engine.command

class CollectCommand(
    val url: String,
    val siteInfo: SiteInfo,
    val extractCommand: ExtractCommand,
)