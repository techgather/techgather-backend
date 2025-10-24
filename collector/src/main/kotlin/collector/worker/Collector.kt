package collector.worker

import collector.engine.CollectEngine
import collector.engine.command.CollectCommand
import collector.engine.command.ExtractCommand

class Collector(
    val name: String,
    val collectionTask: CollectionTask,
    val engine: CollectEngine,
) {

    suspend fun collectWork() {

        val command = CollectCommand(
            name = name,
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