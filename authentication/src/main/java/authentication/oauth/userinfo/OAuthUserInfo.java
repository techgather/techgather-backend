package authentication.oauth.userinfo;

import domain.entity.AuthProvider;

public interface OAuthUserInfo {
        String getEmail();
        String getName();
        String getPicture();
        AuthProvider getAuthProvider();
}