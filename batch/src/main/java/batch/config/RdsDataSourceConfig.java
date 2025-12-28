package batch.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@Profile("prod")
@RequiredArgsConstructor
public class RdsDataSourceConfig {

    private final SecretsManagerConfig secretsManagerConfig;

    @Value("${aws.secret-name}")
    private String secretName;

    @Bean
    public DataSource rdsDataSource() {
        Map<String, String> secrets = secretsManagerConfig.getSecrets(secretName);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(secrets.get("url"));
        config.setUsername(secrets.get("username"));
        config.setPassword(secrets.get("password"));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return new HikariDataSource(config);
    }
}
