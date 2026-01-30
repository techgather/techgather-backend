package authentication.config.security;

import application.exception.CommonClientErrorCode;
import application.exception.UnAuthorizedException;
import authentication.dto.principal.AuthenticatedUser;
import domain.constants.Role;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String CLAIM_USER_ID = "user_id";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_SUB = "sub";

    @Override
    public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {
        Long userId = getUserId(jwt);
        Role role = getRole(jwt);
        String sub = jwt.getClaimAsString(CLAIM_SUB);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role.getAuthority()));

        return new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(userId, sub, role),
                jwt,
                authorities
        );
    }

    private Long getUserId(Jwt jwt) {
        String userIdStr = jwt.getClaimAsString(CLAIM_USER_ID);
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new UnAuthorizedException(CommonClientErrorCode.UNAUTHORIZED, e);
        }
    }

    private Role getRole(Jwt jwt) {
        String role = jwt.getClaimAsString(CLAIM_ROLE);
        try {
            return Role.fromAuthority(role);
        } catch (IllegalArgumentException e) {
            throw new UnAuthorizedException(CommonClientErrorCode.UNAUTHORIZED, e);
        }
    }
}
