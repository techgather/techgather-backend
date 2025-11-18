package authentication.config;

import authentication.domain.OAuthUserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * Congito 이용한 OIDC 로그인 처리
 */
@Component
public class CognitoOidcHandler implements OAuth2ProviderHandler{
    @Override
    public boolean supports(String registrationId) {
        return registrationId.equals("cognito");
    }

    @Override
    public OAuthUserInfo extractUserInfo(OAuth2User user) {
        return new OAuthUserInfo(
                user.getAttribute("email"),
                user.getAttribute("given_name"),
                user.getAttribute("picture")
        );
    }
}
