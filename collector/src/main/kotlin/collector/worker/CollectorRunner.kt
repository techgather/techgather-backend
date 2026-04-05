package collector.worker

import collector.worker.config.CollectorRunProperties
import io.ktor.network.tls.TLSException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CollectorRunner(
    private val collectorRegistry: CollectorRegistry,
    private val runProperties: CollectorRunProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun runAtStartup() {
        if (!runProperties.runOnStartup) {
            return
        }
        runCatching { runCollectors() }
            .onFailure { e -> log.error("Collector startup run failed", e) }
    }

    @Scheduled(
        cron = "\${collector.run.cron:0 0 3 * * *}",
        zone = "\${collector.run.zone:Asia/Seoul}"
    )
    fun run() {
        if (!runProperties.scheduledEnabled) {
            return
        }
        runCatching { runCollectors() }
            .onFailure { e -> log.error("Collector scheduled run failed", e) }
    }

    private fun runCollectors() {
        runBlocking {
            coroutineScope {
                collectorRegistry.getCollectors().map { collector ->
                    async {
                        runCatching { collector.collectWork() }
                            .onFailure { e ->
                                val rootCause = e.rootCause()
                                if (rootCause is TLSException) {
                                    log.warn("Collector skipped by TLS protocol mismatch. target={}, reason={}", collector.name, rootCause.message)
                                } else {
                                    log.error("Collector failed. target={}", collector.name, e)
                                }
                            }
                    }
                }.awaitAll()
            }
        }
    }

    private fun Throwable.rootCause(): Throwable {
        var cause: Throwable = this
        while (cause.cause != null && cause.cause !== cause) {
            cause = cause.cause!!
        }
        return cause
    }
}
