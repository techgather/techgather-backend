package authentication.controller.dto;


import domain.entity.AuthProvider;
import domain.entity.Role;
import domain.entity.User;

public record UserProfileDto(
        Long id,
        String email,
        String name,
        String picture,
        AuthProvider provider,
        Role role
) {
    public static UserProfileDto from(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPicture(),
                user.getProvider(),
                user.getRole()
        );
    }
}