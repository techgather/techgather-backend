package authentication.controller.dto;

public record AuthResponse(
        Long id,
        UserProfileDto userProfile,
        String accessToken,
        String refreshToken
) {}
