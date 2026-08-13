package collector.worker

import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BatchJobTriggerClientTest {

    private val objectMapper = ObjectMapper()

    @Test
    fun `parses ingest counts while ignoring additional batch response fields`() {
        val responseBody = """
            {
              "jobId": 915,
              "jobExecutionId": 976,
              "jobStatus": "COMPLETED",
              "exitCode": "COMPLETED",
              "exitDescription": "",
              "steps": [
                {
                  "stepName": "RSS_FEED_COLLECT_step",
                  "status": "COMPLETED",
                  "exitCode": "COMPLETED",
                  "readCount": 0,
                  "writeCount": 0,
                  "commitCount": 1,
                  "rollbackCount": 0,
                  "uniquePostCount": 0,
                  "insertedPostCount": 0
                }
              ],
              "uniquePostCount": 0,
              "insertedPostCount": 0,
              "failureMessages": []
            }
        """.trimIndent()

        val summary = objectMapper.readValue(
            responseBody,
            BatchJobTriggerClient.PostIngestSummary::class.java
        )

        assertEquals("COMPLETED", summary.jobStatus)
        assertEquals(0L, summary.uniquePostCount)
        assertEquals(0L, summary.insertedPostCount)
        assertEquals(0L, assertNotNull(summary.steps.single()).readCount)
    }
}
