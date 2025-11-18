package authentication.service;

import authentication.domain.AuthProvider;
import authentication.domain.ExternalProvider;
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
import java.util.List;
import java.util.Map;

/**
 * Cognito를 이용한 OIDC 로그인 서비스
 *
 * - 로그인 성공 후 기존 사용자는 사용자 정보를 갱신
 * - 최초 로그인 사용자는 DB에 회원으로 저장하며
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
        ExternalProvider externalProvider = getExternalProvider(claims);

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
                            .provider(AuthProvider.COGNITO)
                            .externalProvider(externalProvider)
                            .role(Role.USER)
                            .createdAt(LocalDateTime.now())
                            .lastLoginAt(LocalDateTime.now())
                            .build());
                });

        return oidcUser;
    }

    /*
        identities json 구조 예시
        "identities": [
        {
          "dateCreated": "1762595640793",
          "userId": "116014585346982261063",
          "providerName": "Google",
          "providerType": "Google",
          "issuer": null,
          "primary": "true"
        }
  ]
     */
    private ExternalProvider getExternalProvider(Map<String, Object> claims) {
        @SuppressWarnings("unchecked")
        Map<String, Object> identities = (Map<String, Object>) ((List) claims.get("identities")).get(0);

        String externalProvider = (String) identities.get("providerName");
        ExternalProvider provider = ExternalProvider.from(externalProvider);

        if (provider == ExternalProvider.UNKNOWN) {
            log.error("Unknown External Provider: {}", identities);
        }
        return provider;
    }
}
