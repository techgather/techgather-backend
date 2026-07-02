package collector

import collector.worker.config.CollectorRunProperties
import collector.worker.config.BatchJobTriggerProperties
import collector.worker.config.TargetProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableConfigurationProperties(value = [TargetProperties::class, CollectorRunProperties::class, BatchJobTriggerProperties::class])
@EnableScheduling
class CollectorApplication

fun main(args: Array<String>) {
    runApplication<CollectorApplication>(*args)
}
