package authentication.dto;

import authentication.userinfo.CustomOAuthUserInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

// null인 경우 json 미포함
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        @JsonProperty("user_info")
        CustomOAuthUserInfo userInfo,

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("access_token_expires_at")
        Instant accessTokenExpiresAt,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("id_token")
        String idToken
) {}
