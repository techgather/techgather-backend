package api.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region.AP_NORTHEAST_2
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.ssm.SsmClient

@Configuration
class AwsConfig {

    @Bean
    @Profile("local")
    fun awsCredentialsProviderDev(): AwsCredentialsProvider {
        return ProfileCredentialsProvider.create("tg-dev")
    }

    @Bean
    @Profile("!local")
    fun awsCredentialsProviderProd(
        @Value("\${spring.cloud.aws.credentials.access-key}") accessKey: String,
        @Value("\${spring.cloud.aws.credentials.secret-key}") secretKey: String
    ): AwsCredentialsProvider {
        return StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKey, secretKey)
        )
    }

    @Bean
    fun ssmClient(
        awsCredentialsProvider: AwsCredentialsProvider
    ): SsmClient {
        return SsmClient.builder()
            .region(AP_NORTHEAST_2)
            .credentialsProvider(awsCredentialsProvider)
            .build()
    }

    @Bean
    fun secretsManagerClient(
        awsCredentialsProvider: AwsCredentialsProvider
    ): SecretsManagerClient {
        return SecretsManagerClient.builder()
            .region(AP_NORTHEAST_2)
            .credentialsProvider(awsCredentialsProvider)
            .build()
    }
}