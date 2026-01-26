package authentication.dto.response;

import authentication.oauth.userinfo.CustomOAuthUserInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String accessToken,

        @JsonProperty("user_info")
        CustomOAuthUserInfo userInfo,

        @JsonProperty("access_token_expires_at")
        Instant accessTokenExpiresAt,

        @JsonProperty("id_token")
        String idToken
) {}
