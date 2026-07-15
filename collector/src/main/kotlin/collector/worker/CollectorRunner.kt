package collector.worker

import application.notification.DiscordNotifier
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
    private val batchJobTriggerClient: BatchJobTriggerClient,
    private val discordNotifier: DiscordNotifier,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun runAtStartup() {
        if (!runProperties.runOnStartup) {
            return
        }
        runCatching { runCollectors() }
            .onFailure { e ->
                log.error("Collector startup run failed", e)
                notifyUnexpectedFailure(e)
            }
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
            .onFailure { e ->
                log.error("Collector scheduled run failed", e)
                notifyUnexpectedFailure(e)
            }
    }

    private fun runCollectors() {
        runBlocking {
            coroutineScope {
                val collectors = collectorRegistry.getCollectors()
                val outcomes = collectors.map { collector ->
                    async {
                        try {
                            val collectedCount = collector.collectWork()
                            CollectorOutcome(collector.name, collectedCount, null)
                        } catch (e: Throwable) {
                            val rootCause = e.rootCause()
                            if (rootCause is TLSException) {
                                log.warn("Collector skipped by TLS protocol mismatch. target={}, reason={}", collector.name, rootCause.message)
                            } else {
                                log.error("Collector failed. target={}", collector.name, e)
                            }
                            CollectorOutcome(collector.name, 0, rootCause)
                        }
                    }
                }.awaitAll()
                val ingestResult = batchJobTriggerClient.triggerPostIngest()
                notifyCollectionAndIngestResult(outcomes, ingestResult)
                log.info(
                    "All collectors finished. total={}, succeeded={}, failed={}",
                    outcomes.size,
                    outcomes.count { it.failure == null },
                    outcomes.count { it.failure != null }
                )
            }
        }
    }

    private fun notifyCollectionAndIngestResult(
        outcomes: List<CollectorOutcome>,
        ingestResult: BatchJobTriggerClient.PostIngestTriggerResult
    ) {
        val failures = outcomes.filter { it.failure != null }
        val ingestFailed = !ingestResult.skipped && !ingestResult.completed
        val title = when {
            failures.isNotEmpty() || ingestFailed -> "⚠️ 게시글 수집 처리 일부 실패"
            ingestResult.skipped -> "⚠️ 게시글 수집 처리 미완료"
            else -> "✅ 게시글 수집 완료"
        }

        val message = buildString {
            appendLine("처리 대상 사이트: ${outcomes.size}개")
            val finalPostCount = if (ingestResult.completed) {
                ingestResult.summary?.uniquePostCount
            } else {
                null
            }
            appendLine("최종 처리 게시글: ${finalPostCount?.toString() ?: "확인 불가"}건")
            if (failures.isNotEmpty()) {
                appendLine("실패 대상:")
                failures.forEach { outcome ->
                    appendLine("- ${outcome.name}: ${outcome.failure?.message ?: "알 수 없는 오류"}")
                }
            }
            when {
                ingestResult.skipped -> appendLine("처리 상태: 배치 스킵")
                ingestResult.completed -> appendLine("처리 상태: 완료")
                else -> {
                    appendLine("처리 상태: 실패 (HTTP ${ingestResult.statusCode ?: "연결 실패"})")
                    ingestResult.errorMessage?.let { appendLine("실패 사유: $it") }
                    ingestResult.summary?.failureMessages
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { appendLine("실패 내용: ${it.joinToString(" | ")}") }
                }
            }
        }

        discordNotifier.send(title, message)
    }

    private fun notifyUnexpectedFailure(e: Throwable) {
        val rootCause = e.rootCause()
        discordNotifier.send(
            "❌ 수집 파이프라인 실패",
            "오류: ${rootCause.message ?: rootCause::class.simpleName}"
        )
    }

    private data class CollectorOutcome(
        val name: String,
        val collectedCount: Int,
        val failure: Throwable?,
    )

    private fun Throwable.rootCause(): Throwable {
        var cause: Throwable = this
        while (cause.cause != null && cause.cause !== cause) {
            cause = cause.cause!!
        }
        return cause
    }
}
