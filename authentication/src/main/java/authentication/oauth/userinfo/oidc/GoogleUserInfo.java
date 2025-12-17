package authentication.oauth.userinfo.oidc;

import authentication.oauth.userinfo.OAuthUserInfo;
import domain.entity.AuthProvider;

import java.util.Map;

public class GoogleUserInfo implements OAuthUserInfo {
    private final Map<String, Object> attributes;
    private final AuthProvider authProvider;

    public GoogleUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.authProvider = AuthProvider.GOOGLE;
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("given_name");
    }

    @Override
    public String getPicture() {
        return (String) attributes.get("picture");
    }

    @Override
    public AuthProvider getAuthProvider() {
        return this.authProvider;
    }
}
