package authentication.oauth.userinfo.oidc;

import authentication.oauth.userinfo.OAuthUserInfo;
import authentication.oauth.userinfo.OAuthUserInfoCreator;
import domain.entity.AuthProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GoogleUserInfoCreator implements OAuthUserInfoCreator {
    @Override
    public boolean supports(AuthProvider provider) {
        return AuthProvider.GOOGLE == provider;
    }

    @Override
    public OAuthUserInfo create(Map<String, Object> attributes) {
        return new GoogleUserInfo(attributes);
    }
}
