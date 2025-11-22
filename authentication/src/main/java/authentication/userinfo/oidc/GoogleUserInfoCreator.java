package authentication.userinfo.oidc;

import authentication.domain.AuthProvider;
import authentication.userinfo.OAuthUserInfo;
import authentication.userinfo.OAuthUserInfoCreator;
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
