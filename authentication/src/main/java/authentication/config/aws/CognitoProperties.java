package authentication.config.aws;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration")
public class CognitoProperties {

    private Registration cognito;      // 유저용 (yml의 cognito: 항목)
    private Registration cognitoAdmin; // 관리자용 (yml의 cognito-admin: 항목)

    @Getter
    @Setter
    public static class Registration {
        private String clientId;
        private String clientSecret;
    }

    public String getUserClientId() {
        return cognito.getClientId();
    }

    public String getAdminClientId() {
        return cognitoAdmin.getClientId();
    }
}
