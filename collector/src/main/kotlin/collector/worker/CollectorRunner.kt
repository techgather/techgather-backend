package collector.worker

import application.notification.DiscordNotifier
import application.notification.DiscordNotification
import application.notification.DiscordNotification.Severity
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
        val startedAtNanos = System.nanoTime()
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
                val elapsedMillis = (System.nanoTime() - startedAtNanos) / 1_000_000
                notifyCollectionAndIngestResult(outcomes, ingestResult, elapsedMillis)
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
        ingestResult: BatchJobTriggerClient.PostIngestTriggerResult,
        elapsedMillis: Long
    ) {
        val failures = outcomes.filter { it.failure != null }
        val ingestFailed = !ingestResult.skipped && !ingestResult.completed
        val severity = when {
            failures.size == outcomes.size || ingestFailed -> Severity.ERROR
            failures.isNotEmpty() || ingestResult.skipped -> Severity.WARNING
            else -> Severity.SUCCESS
        }
        val title = when (severity) {
            Severity.SUCCESS -> "게시글 수집 완료"
            Severity.WARNING -> "게시글 수집 부분 완료"
            else -> "게시글 수집 실패"
        }
        val successCount = outcomes.size - failures.size
        val newPostCount = outcomes.sumOf { it.collectedCount }
        val finalPostCount = ingestResult.summary?.uniquePostCount
        val topSites = outcomes
            .asSequence()
            .filter { it.failure == null && it.collectedCount > 0 }
            .sortedByDescending { it.collectedCount }
            .take(5)
            .joinToString("\n") { "• **${it.name}** — ${it.collectedCount}건" }
            .ifBlank { "신규 글 없음" }
        val failureSummary = failures
            .take(10)
            .joinToString("\n") {
                "• **${it.name}** — ${it.failure?.message ?: "알 수 없는 오류"}"
            }
            .let {
                if (failures.size > 10) "$it\n• 외 ${failures.size - 10}개 사이트" else it
            }
        val ingestStatus = when {
            ingestResult.skipped -> "스킵"
            ingestResult.completed -> "완료"
            else -> "실패 · HTTP ${ingestResult.statusCode ?: "연결 실패"}"
        }

        val notification = DiscordNotification.builder(severity, title)
            .description("기술 블로그 수집 및 게시글 적재 결과입니다.")
            .field("수집 성공", "$successCount / ${outcomes.size}개", true)
            .field("수집 실패", "${failures.size}개", true)
            .field("신규 글", "${newPostCount}건", true)
            .field("최종 적재", finalPostCount?.let { "${it}건" } ?: "확인 불가", true)
            .field("배치 상태", ingestStatus, true)
            .field("소요 시간", "%.1f초".format(elapsedMillis / 1_000.0), true)
            .field("상위 수집 사이트", topSites)
            .apply {
                if (failures.isNotEmpty()) {
                    field("실패 사이트", failureSummary)
                }
                val ingestFailure = buildList {
                    ingestResult.errorMessage?.let(::add)
                    addAll(ingestResult.summary?.failureMessages.orEmpty())
                }.joinToString("\n")
                if (ingestFailure.isNotBlank()) {
                    field("배치 실패 내용", ingestFailure)
                }
            }
            .footer("TechGather · collector")
            .build()

        discordNotifier.send(notification)
    }

    private fun notifyUnexpectedFailure(e: Throwable) {
        val rootCause = e.rootCause()
        discordNotifier.send(
            DiscordNotification.builder(Severity.ERROR, "수집 파이프라인 실패")
                .field("오류", rootCause.message ?: rootCause::class.simpleName ?: "알 수 없는 오류")
                .footer("TechGather · collector")
                .build()
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
