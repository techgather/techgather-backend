package api.global.config

import application.exception.CommonServerErrorCode
import application.exception.TechGatherException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest

@Configuration
class SecretsManagerConfig(
    private val secretsManagerClient: SecretsManagerClient,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val log = LoggerFactory.getLogger(SecretsManagerConfig::class.java)
    }

    fun getSecrets(secretName: String): Map<String, String> {
        try {
            val request = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build()

            val response = secretsManagerClient.getSecretValue(request)
            val secretString = response.secretString()
            return objectMapper.readValue(secretString, object : TypeReference<Map<String, String>>() {})
        } catch (e: Exception) {
            log.error("SecretsManager로부터 보안 암호를 찾지 못했습니다.", e.cause)
            throw TechGatherException(CommonServerErrorCode.NOT_FOUND_PROPERTY, e)
        }
    }
}