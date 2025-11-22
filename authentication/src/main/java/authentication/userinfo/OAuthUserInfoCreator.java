package authentication.userinfo;

import authentication.domain.AuthProvider;

import java.util.Map;

public interface OAuthUserInfoCreator {
    boolean supports(AuthProvider provider);
    OAuthUserInfo create(Map<String, Object> attributes);
}