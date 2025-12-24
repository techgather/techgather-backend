package authentication.controller.dto;


import domain.entity.AuthProvider;
import domain.entity.Role;
import domain.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserProfile(
        @NotNull Long id,
        @NotBlank String email,
        @NotBlank String name,
        String picture,
        @NotNull AuthProvider provider,
        @NotNull Role role
) {
    public static UserProfile from(User user) {
        return new UserProfile(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPicture(),
                user.getProvider(),
                user.getRole()
        );
    }
}