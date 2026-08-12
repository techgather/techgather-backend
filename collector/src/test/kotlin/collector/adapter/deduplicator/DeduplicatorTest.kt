package collector.adapter.deduplicator

import collector.engine.model.ExtractedMessage
import collector.engine.port.ExistingPostLookupPort
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import java.time.LocalDateTime

class DeduplicatorTest {

    @Test
    fun `removes duplicate and already persisted urls`() = runBlocking {
        val lookup = FakeExistingPostLookupPort(setOf("https://example.com/existing"))
        val deduplicator = Deduplicator(lookup)

        val result = deduplicator.deduplicate(
            listOf(
                message(" https://example.com/new "),
                message("https://example.com/new"),
                message("https://example.com/existing"),
            )
        )

        assertEquals(listOf("https://example.com/new"), result.map { it.url })
        assertEquals(
            listOf("https://example.com/new", "https://example.com/existing"),
            lookup.requestedUrls
        )
    }

    @Test
    fun `publishes unique messages when persisted lookup fails`() = runBlocking {
        val deduplicator = Deduplicator(
            object : ExistingPostLookupPort {
                override suspend fun findExistingUrls(urls: List<String>): Set<String> {
                    throw IllegalStateException("batch unavailable")
                }
            }
        )

        val result = deduplicator.deduplicate(
            listOf(
                message("https://example.com/1"),
                message("https://example.com/1"),
                message("https://example.com/2"),
            )
        )

        assertEquals(
            listOf("https://example.com/1", "https://example.com/2"),
            result.map { it.url }
        )
    }

    private fun message(url: String) = ExtractedMessage(
        title = "title",
        url = url,
        pubDate = LocalDateTime.now(),
        tags = emptyList(),
        description = null,
        thumbnail = null,
    )

    private class FakeExistingPostLookupPort(
        private val existingUrls: Set<String>,
    ) : ExistingPostLookupPort {
        var requestedUrls: List<String> = emptyList()

        override suspend fun findExistingUrls(urls: List<String>): Set<String> {
            requestedUrls = urls
            return existingUrls
        }
    }
}
