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
 * OIDC 사용자 정보 로딩 및 사용자 DB 동기화 담당 서비스
 * - ID/Access Token 기반으로 OidcUser 생성
 * - 사용자 존재 여부 확인 후, 신규 가입 또는 마지막 로그인 시간 갱신
 * - OIDC 공급자 식별
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest req) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(req);
        Map<String, Object> claims = oidcUser.getClaims();

        String email = (String) claims.get("email");
        String name  = (String) claims.getOrDefault("name", claims.getOrDefault("given_name", ""));
        String picture = (String) claims.getOrDefault("picture", "");
        String provider = extractProvider(oidcUser);

        userRepository.findByEmail(email)
                .map(existing -> {
                    log.info("User login: {}", existing.getEmail());
                    existing.updateLastLogin(LocalDateTime.now());
                    return existing;
                })
                .orElseGet(() -> {
                    log.info("New user registration: {}", email);
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
