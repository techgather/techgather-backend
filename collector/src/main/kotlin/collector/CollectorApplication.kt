package collector

import application.notification.DiscordNotificationConfig
import collector.worker.config.CollectorRunProperties
import collector.worker.config.BatchJobTriggerProperties
import collector.worker.config.TargetProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.context.annotation.Import

@SpringBootApplication
@EnableConfigurationProperties(value = [TargetProperties::class, CollectorRunProperties::class, BatchJobTriggerProperties::class])
@EnableScheduling
@EnableAsync
@Import(DiscordNotificationConfig::class)
class CollectorApplication

fun main(args: Array<String>) {
    runApplication<CollectorApplication>(*args)
}
