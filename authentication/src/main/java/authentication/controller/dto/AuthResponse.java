package authentication.controller.dto;

import authentication.oauth.userinfo.OAuthUserInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record AuthResponse(
        @JsonProperty("user_info")
        OAuthUserInfo userInfo,

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("access_token_expires_at")
        Instant accessTokenExpiresAt,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("id_token")
        String idToken
) {}
