package authentication.infra.impl;

import authentication.infra.DbSecretLoader;
import authentication.infra.dto.DbSecretProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Component
@Profile("prod")
@RequiredArgsConstructor
public class AwsDbSecretLoader implements DbSecretLoader {

    private final SecretsManagerClient secretsManagerClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.secrets.db-key-name}")
    private String secretName;

    private DbSecretProperties properties;

    @PostConstruct
    public void init() throws JsonProcessingException {
        GetSecretValueResponse response =
                secretsManagerClient.getSecretValue(
                        GetSecretValueRequest.builder()
                                .secretId(secretName)
                                .build()
                );

        this.properties = objectMapper.readValue(response.secretString(), DbSecretProperties.class);
    }

    @Override
    public DbSecretProperties load() {
        return properties;
    }
}
