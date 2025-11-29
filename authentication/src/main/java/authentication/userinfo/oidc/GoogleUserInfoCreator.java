package authentication.userinfo.oidc;

import authentication.domain.AuthProvider;
import authentication.userinfo.CustomOAuthUserInfo;
import authentication.userinfo.CustomOAuthUserInfoCreator;
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
