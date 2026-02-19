package authentication.config.security;

import application.exception.CommonClientErrorCode;
import application.exception.UnAuthorizedException;
import authentication.dto.principal.AuthenticatedUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String CLAIM_SUB = "sub";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_COGNITO_GROUPS = "cognito:groups";

    @Override
    public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {
        String sub = getSub(jwt);
        String username = jwt.getClaimAsString(CLAIM_USERNAME);
        List<String> cognitoGroups = getCognitoGroups(jwt);
        List<GrantedAuthority> authorities = getAuthorities(jwt);

        return new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(sub, username, cognitoGroups),
                jwt,
                authorities
        );
    }

    private String getSub(Jwt jwt) {
        String sub = jwt.getClaimAsString(CLAIM_SUB);
        if (sub == null || sub.isBlank()) {
            throw new UnAuthorizedException(CommonClientErrorCode.UNAUTHORIZED, null);
        }
        return sub;
    }

    private List<GrantedAuthority> getAuthorities(Jwt jwt) {
        List<String> groups = getCognitoGroups(jwt);
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (groups != null) {
            for (String group : groups) {
                if (group != null && !group.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + group));
                }
            }
        }

        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return authorities;
    }

    private List<String> getCognitoGroups(Jwt jwt) {
        List<String> groups = jwt.getClaimAsStringList(CLAIM_COGNITO_GROUPS);
        return groups == null ? List.of() : groups;
    }
}
