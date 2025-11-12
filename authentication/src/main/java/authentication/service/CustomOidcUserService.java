package authentication.service;

import authentication.domain.Role;
import authentication.domain.User;
import authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/*
    id token, access token으로 사용자 정보(OidcUser) 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest req) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(req);
        Map<String, Object> c = oidcUser.getClaims();

        String email = (String) c.get("email");
        String name  = (String) c.getOrDefault("name", c.getOrDefault("given_name", ""));
        String picture = (String) c.getOrDefault("picture", "");
        String provider = extractProvider(oidcUser);

        userRepository.findByEmail(email)
                .map(existing -> {
                    log.info("기존 사용자 로그인: {}", existing.getEmail());
                    existing.updateLastLogin(LocalDateTime.now());
                    return existing;
                })
                .orElseGet(() -> {
                    log.info("신규 사용자 가입: {}", email);
                    return userRepository.save(User.builder()
                            .email(email)
                            .name(name)
                            .picture(picture)
                            .provider(provider)
                            .role(Role.USER)
                            .createdAt(LocalDateTime.now())
                            .lastLoginAt(LocalDateTime.now())
                            .build());
                });

        return oidcUser;
    }

    /*
        google provider의 username은 google_로 시작
        identities claim 활용 가능
     */
    private String extractProvider(OidcUser user) {
        String username = (String) user.getClaims().getOrDefault("username", "");
        if (username.startsWith("google_")) return "GOOGLE";
        return "COGNITO";
    }
}
