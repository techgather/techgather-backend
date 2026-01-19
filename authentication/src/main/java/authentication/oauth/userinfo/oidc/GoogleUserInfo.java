package authentication.oauth.userinfo.oidc;

import authentication.oauth.userinfo.CustomOAuthUserInfo;
import domain.constants.AuthProvider;
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
    public String getSubject() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        String firstName = (String) attributes.get("given_name");
        String lastName = (String) attributes.get("family_name");

        if (firstName != null && lastName != null) {
            return lastName + firstName;
        }
        return (String) attributes.get("name");
    }

    @Override
    public String getPicture() {
        return (String) attributes.get("picture");
    }

    @Override
    public domain.constants.AuthProvider getAuthProvider() {
        return this.authProvider;
    }
}
