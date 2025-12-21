package authentication.config;

import authentication.infra.DbSecretLoader;
import authentication.infra.dto.DbSecretProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

    private final DbSecretLoader dbSecretLoader;

    @Bean
    public DataSource dataSource() throws JsonProcessingException {
        DbSecretProperties properties = dbSecretLoader.load();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getUrl());
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        return new HikariDataSource(config);
    }
}
