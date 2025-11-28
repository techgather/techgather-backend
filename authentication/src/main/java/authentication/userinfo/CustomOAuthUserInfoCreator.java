package authentication.userinfo;

import authentication.domain.AuthProvider;

import java.util.Map;

public interface CustomOAuthUserInfoCreator {
    boolean supports(AuthProvider provider);
    CustomOAuthUserInfo create(Map<String, Object> attributes);
}