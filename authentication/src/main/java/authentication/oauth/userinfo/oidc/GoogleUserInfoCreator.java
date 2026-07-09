package authentication.oauth.userinfo.oidc;

import authentication.oauth.userinfo.CustomOAuthUserInfo;
import authentication.oauth.userinfo.CustomOAuthUserInfoCreator;
import domain.constants.AuthProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GoogleUserInfoCreator implements CustomOAuthUserInfoCreator {
    @Override
    public boolean supports(AuthProvider provider) {
        return AuthProvider.GOOGLE == provider;
    }

    @Override
    public CustomOAuthUserInfo create(Map<String, Object> attributes) {
        return new GoogleUserInfo(attributes);
    }
}
