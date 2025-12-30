package batch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.ssm.SsmClient;

@Configuration
public class AwsConfig {

    @Bean
    public SsmClient ssmClient() {
        return SsmClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(awsCredentialsProvider())
                .build();
    }

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        return AwsCredentialsProviderChain.builder()
                .addCredentialsProvider(ProfileCredentialsProvider.create())
                .addCredentialsProvider(ContainerCredentialsProvider.builder().build())
                .addCredentialsProvider(WebIdentityTokenFileCredentialsProvider.builder().build())
                .build();
    }

    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
                .credentialsProvider(awsCredentialsProvider())
                .region(Region.AP_NORTHEAST_2).build();
    }
}
