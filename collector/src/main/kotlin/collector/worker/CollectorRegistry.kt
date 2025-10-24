package collector.worker

import collector.worker.config.TargetProperties
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CollectorRegistry(
    private val props: TargetProperties,
    private val collectorFactory: CollectorFactory,
) {

    private val collectors = mutableListOf<Collector>()

    @PostConstruct
    fun registerWorkers() {
        //worker 등록
        props.entries.forEach { (target, config) ->
            if(props.actives.contains(target)) {
                //worker 생성
                collectors.add(collectorFactory.createCollector(target, config))
                //TODO: worker 생성 실패 시 에러 핸들링
            }
        }
    }

    fun getCollectors(): List<Collector> = collectors
}