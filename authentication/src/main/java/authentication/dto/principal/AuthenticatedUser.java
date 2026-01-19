package authentication.dto.principal;


import domain.constants.Role;

public record AuthenticatedUser(
        Long userId,
        String sub,
        Role role
) {
}
