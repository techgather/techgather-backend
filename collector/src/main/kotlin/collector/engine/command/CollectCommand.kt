package collector.engine.command

class CollectCommand(
    val name: String,
    val url: String,
    val extractCommand: ExtractCommand,
)