package authentication.oauth.service;

import authentication.oauth.userinfo.CustomOAuthUserInfo;
import authentication.oauth.userinfo.UserInfoFactory;
import authentication.oauth.userinfo.oidc.CustomOidcUser;
import authentication.service.CognitoAuthService;
import domain.constants.AuthProvider;
import domain.constants.Role;
import domain.entity.User;
import domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final UserInfoFactory userInfoFactory;
    private final CognitoAuthService cognitoAuthService;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest req) throws OAuth2AuthenticationException {
        // 0. 기본 OidcUser 로드 (Cognito로부터 정보 획득)
        OidcUser oidcUser = super.loadUser(req);

        // 1. 컨텍스트 추출 및 유저 정보 객체화
        String registrationId = req.getClientRegistration().getRegistrationId();
        AuthProvider authProvider = getAuthProvider(oidcUser.getClaims());
        CustomOAuthUserInfo userInfo = userInfoFactory.create(authProvider, oidcUser.getAttributes());

        // 2. 권한 결정 (Admin 경로 진입 시 Cognito 그룹 유효성 검사 포함)
        Role targetRole = determineRole(registrationId, oidcUser);

        // 3. 서비스 DB 동기화
        User user = processUserRegistration(userInfo, targetRole);

        // 4. 최종 인증 객체 반환 (registrationId를 포함하여 리프레시 토큰 등 후속 처리 지원)
        return new CustomOidcUser(oidcUser, userInfo, user.getRole());
    }

    /**
     * 유저 정보를 DB에 동기화하고, 신규 유저일 경우 그룹 할당 등 가입 절차 진행
     */
    private User processUserRegistration(CustomOAuthUserInfo userInfo, Role targetRole) {
        String sub = userInfo.getSubject();
        String email = userInfo.getEmail();

        if (sub == null || sub.isBlank()) {
            throw new OAuth2AuthenticationException("OAuth2 공급자로부터 sub를 불러올 수 없습니다.");
        }

        if (email == null) {
            throw new OAuth2AuthenticationException("OAuth2 공급자로부터 이메일을 불러올 수 없습니다.");
        }

        return userRepository.findById(sub)
                .map(userInfo::updateEntity)
                .or(() -> userRepository.findByEmail(email).map(userInfo::updateEntity))
                .orElseGet(() -> registerNewUser(userInfo, targetRole, userInfo.getSubject()));
    }

    /**
     * 신규 사용자 등록 - 관리자는 그룹화 생략, 일반 유저는 USER 그룹 자동 추가
     */
    private User registerNewUser(CustomOAuthUserInfo userInfo, Role targetRole, String sub) {
        if (targetRole == Role.USER) {
            cognitoAuthService.addUserToDefaultGroup(sub);
        }
        return userRepository.save(userInfo.toEntity());
    }

    /**
     * 접근 경로(registrationId)에 따른 최종 Role 결정
     */
    private Role determineRole(String registrationId, OidcUser oidcUser) {
        if (OAuthClientRegistration.COGNITO_ADMIN.equals(registrationId)) {
            validateAdminGroup(oidcUser);
            return Role.ADMIN;
        }
        return Role.USER;
    }

    /**
     * Cognito 토큰 내에 ADMIN 그룹 포함 여부 검증
     */
    private void validateAdminGroup(OidcUser oidcUser) {
        List<String> groups = oidcUser.getClaimAsStringList("cognito:groups");

        if (groups == null || !groups.contains("ADMIN")) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("not_admin", "해당 계정은 관리자 그룹에 속해있지 않습니다.", null));
        }
    }

    /**
     * 소셜 로그인 공급자 식별
     */
    private AuthProvider getAuthProvider(Map<String, Object> claims) {
        Object identitiesObj = claims.get("identities");

        if (!(identitiesObj instanceof List<?> list) || list.isEmpty()) {
            return AuthProvider.COGNITO;
        }

        return list.stream()
                .filter(Map.class::isInstance)
                .map(m -> (Map<?, ?>) m)
                .filter(m -> m.get("providerName") instanceof String)
                .map(m -> AuthProvider.from((String) m.get("providerName")))
                .findFirst()
                .orElse(AuthProvider.COGNITO);
    }
}
