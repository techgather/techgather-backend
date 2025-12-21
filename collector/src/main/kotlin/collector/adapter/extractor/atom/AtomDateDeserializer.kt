package collector.adapter.extractor.atom

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AtomDateDeserializer : JsonDeserializer<LocalDateTime>() {

    private val zone = ZoneId.of("Asia/Seoul")

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): LocalDateTime {
        val text = p.text.trim()

        return try {
            OffsetDateTime.parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .atZoneSameInstant(zone)
                .toLocalDateTime()
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
}