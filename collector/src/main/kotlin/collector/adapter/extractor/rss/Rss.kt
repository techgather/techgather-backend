package collector.adapter.extractor.rss

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class Rss(
    val channel: Channel
,    val version: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Channel(
    val title: String,
    val link: String,
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    val items: List<Item>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Item(
    val title: String,
    val link: String,
    @JsonDeserialize(using = RssDateDeserializer::class)
    val pubDate: LocalDateTime,
    val description: String?,
    val author: String?,
    @JacksonXmlElementWrapper(useWrapping = false) //<category> 태그 여러개 처리
    @JacksonXmlProperty(localName = "category")
    val categories: List<String>?
)