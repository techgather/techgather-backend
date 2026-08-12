package collector.worker.config

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchJobTriggerPropertiesTest {

    @Test
    fun `derives lookup url from post ingest url`() {
        val properties = BatchJobTriggerProperties(
            postIngestUrl = "http://batch:7500/internal/jobs/post-ingest"
        )

        assertEquals(
            "http://batch:7500/internal/jobs/existing-post-urls",
            properties.resolvedExistingPostUrlsUrl()
        )
    }

    @Test
    fun `uses explicitly configured lookup url`() {
        val properties = BatchJobTriggerProperties(
            postIngestUrl = "http://batch:7500/internal/jobs/post-ingest",
            existingPostUrlsUrl = "http://proxy/internal/existing-post-urls"
        )

        assertEquals(
            "http://proxy/internal/existing-post-urls",
            properties.resolvedExistingPostUrlsUrl()
        )
    }
}
