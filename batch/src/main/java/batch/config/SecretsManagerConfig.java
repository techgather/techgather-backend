package batch.config;

import application.exception.TechGatherException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.Map;

import static application.exception.CommonServerErrorCode.NOT_FOUND_PROPERTY;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecretsManagerConfig {

    private final SecretsManagerClient secretsManagerClient;
    private final ObjectMapper objectMapper;

    public Map<String, String> getSecrets(String secretName) {
        try {
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
            String secretString = response.secretString();
            return objectMapper.readValue(secretString, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("SecretsManager로부터 암호를 가져오지 못했습니다.", e.getCause());
            throw new TechGatherException(NOT_FOUND_PROPERTY, e);
        }
    }

}