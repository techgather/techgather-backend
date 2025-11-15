package authentication.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class CognitoProperties {

    @Value("${spring.security.oauth2.client.registration.cognito.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.cognito.domain-uri}")
    private String domainUri;
}