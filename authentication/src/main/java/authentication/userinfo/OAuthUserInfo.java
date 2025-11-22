package authentication.userinfo;

import authentication.domain.AuthProvider;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface OAuthUserInfo {
        String getEmail();
        String getName();
        String getPicture();
        @JsonProperty("auth_provider") AuthProvider getAuthProvider();
}