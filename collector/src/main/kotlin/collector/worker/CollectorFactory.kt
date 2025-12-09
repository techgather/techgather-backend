package collector.worker

import collector.engine.CollectEngine
import collector.engine.model.Language
import collector.engine.port.DeduplicatePort
import collector.engine.port.Publisher
import collector.worker.config.TargetProps
import collector.worker.provider.AdapterProvider
import org.springframework.stereotype.Component

@Component
class CollectorFactory (
    private val adapterProvider: AdapterProvider,
    private val deduplicatePort: DeduplicatePort,
    private val publisher: Publisher,
) {
    fun createCollector(target: String, config: TargetProps): Collector {

        //adapterProvider 에서 config 에 설정된 crawler 가져오기
        val crawler = adapterProvider.getCrawler(config.adapter.crawler.type)
        //adapterProvider 에서 config 에 설정된 extractor 가져오기
        val extractor = adapterProvider.getExtractor(config.adapter.extractor.type)
        //engine 생성
        val engine = CollectEngine(crawler, extractor, deduplicatePort, publisher)

        return Collector(
            name = target,
            language = Language.from(config.language),
            collectionTask = CollectionTask(
                url = config.url,
                extractTask = ExtractTask(
                    useDefaultThumbnail = config.adapter.extractor.useDefaultThumbnail,
                    defaultThumbnail = config.adapter.extractor.defaultThumbnail
                )
            ),
            engine = engine
        )
    }
}