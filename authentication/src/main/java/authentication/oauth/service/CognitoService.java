package authentication.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class CognitoService {
    private final CognitoIdentityProviderClient cognitoClient;
    private final CognitoClientHelper cognitoClientHelper;

    public void revokeRefreshToken(String refreshToken) {
        cognitoClient.revokeToken(builder -> builder
                .clientId(cognitoClientHelper.getClientId())
                .clientSecret(cognitoClientHelper.getClientSecret())
                .token(refreshToken)
        );
    }
}
