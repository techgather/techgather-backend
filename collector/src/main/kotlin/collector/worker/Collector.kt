package collector.worker

import collector.engine.CollectEngine
import collector.engine.command.CollectCommand
import collector.engine.command.ExtractCommand
import collector.engine.command.SiteInfo
import collector.engine.model.Language

class Collector(
    val name: String,
    val language: Language,
    val collectionTask: CollectionTask,
    val engine: CollectEngine,
) {

    suspend fun collectWork() {

        val command = CollectCommand(
            siteInfo = SiteInfo(
                name = name,
                language = language
            ),
            url = collectionTask.url,
            extractCommand = collectionTask.extractTask.let {
                ExtractCommand(
                    it.useDefaultThumbnail,
                    it.defaultThumbnail
                )
            }
        )

        engine.run(command)
    }
}