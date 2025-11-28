package collector.worker

import kotlin.time.Duration

data class CollectionTask (
    val url: String,
    val extractTask: ExtractTask,
    val interval: Duration? = null,  // 수집 주기
    val enabled: Boolean = true,      // 활성화 여부
    val maxRetry: Int = 3,            // 실패 시 재시도 횟수
)

data class ExtractTask (
    val useDefaultThumbnail: Boolean,
    val defaultThumbnail: String?,
)