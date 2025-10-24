package collector.worker

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class CollectorRunner(
    private val collectorRegistry: CollectorRegistry,
    ): CommandLineRunner {
    override fun run(vararg args: String?) {

        runBlocking {
            coroutineScope {
                collectorRegistry.getCollectors().map { collector ->
                    async {
                        collector.collectWork()
                    }
                    //TODO: worker 실행 실패 시 에러 핸들링
                }.awaitAll()
            }
        }
    }
}