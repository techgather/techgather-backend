package api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider
import software.amazon.awssdk.regions.Region.AP_NORTHEAST_2
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.ssm.SsmClient

@Configuration
class AwsConfig {

    @Bean
    fun ssmClient(): SsmClient {
        return SsmClient.builder()
            .region(AP_NORTHEAST_2)
            .credentialsProvider(awsCredentialsProvider())
            .build()
    }

    @Bean
    fun awsCredentialsProvider(): AwsCredentialsProvider {
        return AwsCredentialsProviderChain.builder()
            .addCredentialsProvider(ProfileCredentialsProvider.create())
            .addCredentialsProvider(ContainerCredentialsProvider.builder().build())
            .addCredentialsProvider(WebIdentityTokenFileCredentialsProvider.builder().build())
            .build()
    }

    @Bean
    fun secretsManagerClient(): SecretsManagerClient {
        return SecretsManagerClient.builder()
            .credentialsProvider(awsCredentialsProvider())
            .region(AP_NORTHEAST_2).build()
    }
}