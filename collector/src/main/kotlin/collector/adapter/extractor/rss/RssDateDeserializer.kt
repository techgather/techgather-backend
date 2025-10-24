package collector.adapter.extractor.rss

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class RssDateDeserializer: JsonDeserializer<LocalDateTime>() {

    private val zone = ZoneId.of("Asia/Seoul")

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): LocalDateTime {
        val text = p.text.trim()
        return ZonedDateTime.parse(text, DateTimeFormatter.RFC_1123_DATE_TIME)
            .withZoneSameInstant(zone)
            .toLocalDateTime()
    }
}