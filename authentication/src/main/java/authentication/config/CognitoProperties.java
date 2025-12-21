package authentication.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cognito")
@Getter
public class CognitoProperties {
    private String clientId;
    private String domainUri;
}