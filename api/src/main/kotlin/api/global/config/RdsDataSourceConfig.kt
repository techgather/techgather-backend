package api.global.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import javax.sql.DataSource

@Configuration
@Profile("prod")
class RdsDataSourceConfig(
    private val secretsManagerConfig: SecretsManagerConfig
) {

    @Value("\${aws.secret-name}")
    private lateinit var secretName: String

    @Bean
    @Primary
    fun dataSource(): DataSource {
        val secrets = secretsManagerConfig.getSecrets(secretName)

        val config = HikariConfig()
        config.setJdbcUrl(secrets["url"])
        config.setUsername(secrets["username"])
        config.setPassword(secrets["password"])
        config.setDriverClassName("com.mysql.cj.jdbc.Driver")
        return HikariDataSource(config)
    }
}