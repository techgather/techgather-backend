package authentication.userinfo.oidc;

import authentication.domain.AuthProvider;
import authentication.userinfo.CustomOAuthUserInfo;
import lombok.ToString;

import java.util.Map;

@ToString
public class GoogleUserInfo implements CustomOAuthUserInfo {
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
