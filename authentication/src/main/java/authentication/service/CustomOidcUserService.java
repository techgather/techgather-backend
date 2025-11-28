package authentication.service;

import authentication.domain.AuthProvider;
import authentication.domain.Role;
import authentication.domain.User;
import authentication.repository.UserRepository;
import authentication.userinfo.CustomOAuthUserInfo;
import authentication.userinfo.UserInfoFactory;
import authentication.userinfo.oidc.CustomOidcUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * OIDC 로그인 후 사용자 정보를 처리하는 Service
 *
 * - Provider(Cognito, Google 등)로부터 사용자 정보 조회
 * - Provider attributes → OAuthUserInfo 공통 모델로 변환
 * - DB에 회원 정보 신규 저장 또는 업데이트 (프로필 & 마지막 로그인 시간)
 * - CustomOidcUser를 반환하여 인증 Principal로 사용
 *  즉, SecurityContext에는 기본 OidcUser가 아닌 OAuthUserInfo가 포함된 CustomOidcUser가 저장
 */
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final UserInfoFactory userInfoFactory;

    @Override
    public OidcUser loadUser(OidcUserRequest req) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(req);
        AuthProvider authProvider = getAuthProvider(oidcUser.getClaims());
        CustomOAuthUserInfo userInfo = userInfoFactory.create(authProvider, oidcUser.getAttributes());

        saveOrUpdate(userInfo);

        return new CustomOidcUser(oidcUser, userInfo);
    }

    @Transactional
    public void saveOrUpdate(CustomOAuthUserInfo userInfo) {

        String email = userInfo.getEmail();
        if (email == null) {
            throw new OAuth2AuthenticationException("Email not provided by provider");
        }

        userRepository.findByEmail(email)
            .map(user -> user.updateFrom(userInfo))   // 기존 회원 -> 업데이트
            .orElseGet(() -> createNewUser(userInfo)); // 신규 회원
    }

    private User createNewUser(CustomOAuthUserInfo userInfo) {
        return userRepository.save(
                User.builder()
                        .email(userInfo.getEmail())
                        .name(userInfo.getName())
                        .picture(userInfo.getPicture())
                        .provider(userInfo.getAuthProvider())
                        .role(Role.USER)
                        .createdAt(LocalDateTime.now())
                        .lastLoginAt(LocalDateTime.now())
                        .build()
        );
    }

    private AuthProvider getAuthProvider(Map<String, Object> claims) {
        Object identitiesObj = claims.get("identities");

        // identities가 비었다면, cognito 직접 로그인
        if (!(identitiesObj instanceof List<?> list) || list.isEmpty()) {
            return AuthProvider.COGNITO;
        }

        // identity와 providerName 타입 체크
        Object first = list.get(0);
        if (!(first instanceof Map<?, ?> identity)){
            return AuthProvider.UNKNOWN;
        }
        if (!(identity.get("providerName") instanceof String providerName)) {
            return AuthProvider.UNKNOWN;
        }

        return AuthProvider.from(providerName);
    }
}
