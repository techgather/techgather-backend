package collector.utils.retry

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import kotlin.math.min

private val log = LoggerFactory.getLogger("retrySuspend")

/**
 * @param maxAttempts:
 */
suspend fun <T> retrySuspend(
    maxAttempts: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 4000,
    multiplier: Double = 2.0,
    block: suspend () -> T,
    recover: (suspend () -> T)? = null,
): T {
    var attempt = 0
    var delayMs = initialDelay

    while (true) {
        try {
            return block()
        } catch (e: Exception) {
            attempt++

            log.warn("Exception occurred while retrying block, exception : " + e.message)

            if (attempt >= maxAttempts) {
                //recover 함수가 있으면 실행
                if(recover != null) {
                    return recover()
                //업으면 에러 throw
                } else {
                    throw e
                }
            }

            delay(delayMs)

            delayMs = min((delayMs * multiplier).toLong(), maxDelay)
        }
    }
}
