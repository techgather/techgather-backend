package authentication.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
public class AwsConfig {

    @Bean
    @Profile("local")
    public AwsCredentialsProvider awsCredentialsProviderLocal() {
        return ProfileCredentialsProvider.create("tg-dev");
    }

    @Bean
    @Profile("!local")
    public AwsCredentialsProvider awsCredentialsProviderProd(
            @Value("${spring.cloud.aws.credentials.access-key}") String accessKey,
            @Value("${spring.cloud.aws.credentials.secret-key}") String secretKey
    ) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );
    }

    @Bean
    public CognitoIdentityProviderClient cognitoIdentityProviderClient(
            AwsCredentialsProvider awsCredentialsProvider
    ) {
        return CognitoIdentityProviderClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }
}

