package authentication.controller.dto;

import domain.entity.User;
import jakarta.validation.constraints.NotNull;

public record AuthTokenResponse(
        @NotNull UserProfile userProfile,
        String accessToken,
        String refreshToken
) {
    public static AuthTokenResponse from(User user, String accessToken, String refreshToken) {
        return new AuthTokenResponse(UserProfile.from(user), accessToken, refreshToken);
    }
}
