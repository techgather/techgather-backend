package collector

import collector.worker.config.TargetProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(TargetProperties::class)
class CollectorApplication

fun main(args: Array<String>) {
    runApplication<CollectorApplication>(*args)
}