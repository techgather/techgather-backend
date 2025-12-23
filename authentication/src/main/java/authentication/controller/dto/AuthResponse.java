package authentication.controller.dto;

import domain.entity.Role;

public record AuthResponse(
        Long id,
        UserProfileDto userProfile,
        String accessToken,
        String refreshToken,
        Role role
) {}
