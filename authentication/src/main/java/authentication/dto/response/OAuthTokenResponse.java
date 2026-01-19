package authentication.dto.response;

import authentication.oauth.userinfo.CustomOAuthUserInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record OAuthTokenResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("user_info")
        CustomOAuthUserInfo userInfo,

        @JsonProperty("access_token_expires_at")
        Instant accessTokenExpiresAt,

        @JsonProperty("id_token")
        String idToken
) {
}
