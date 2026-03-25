package authentication.service;

import authentication.dto.response.OAuthTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CognitoAuthService cognitoAuthService;

    public OAuthTokenResponse refreshToken(String refreshToken, String clientId) {
        return cognitoAuthService.refresh(refreshToken, clientId);
    }

    public OAuthTokenResponse refreshToken(String refreshToken) {
        return cognitoAuthService.refreshWithUserClient(refreshToken);
    }

    public String logout(String userName, String baseUrl) {
        return cognitoAuthService.globalSignOut(userName, baseUrl);
    }
}
