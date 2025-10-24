package collector.adapter.publisher.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(KafkaProperty::class)
open class KafkaConfig {

    @Bean
    open fun objectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(JavaTimeModule())
        }
    }
}
