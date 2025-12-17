package authentication.oauth.userinfo;

import domain.entity.AuthProvider;

import java.util.Map;

public interface OAuthUserInfoCreator {
    boolean supports(AuthProvider provider);
    OAuthUserInfo create(Map<String, Object> attributes);
}