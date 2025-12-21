package collector.adapter.extractor.atom

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.time.LocalDateTime

@JacksonXmlRootElement(
    localName = "feed",
    namespace = "http://www.w3.org/2005/Atom"
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Atom(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(
        localName = "entry",
        namespace = "http://www.w3.org/2005/Atom"
    )
    val entries: List<AtomEntry>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AtomEntry(
    val title: String?,
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "link")
    val links: List<AtomLink>?,
    @JsonDeserialize(using = AtomDateDeserializer::class)
    val updated: LocalDateTime?,
    val summary: String?,
    val content: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AtomLink(
    @JacksonXmlProperty(isAttribute = true, localName = "href")
    val href: String?,
    @JacksonXmlProperty(isAttribute = true, localName = "rel")
    val rel: String? = null,
)
