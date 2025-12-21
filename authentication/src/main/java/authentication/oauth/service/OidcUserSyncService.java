package authentication.oauth.service;

import domain.repository.UserRepository;
import authentication.oauth.userinfo.OAuthUserInfo;
import authentication.oauth.userinfo.UserInfoFactory;
import authentication.oauth.userinfo.oidc.DomainAwareOidcUser;
import domain.entity.AuthProvider;
import domain.entity.Role;
import domain.entity.User;
import domain.vo.OAuthUserProfile;
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

@Service
@RequiredArgsConstructor
public class OidcUserSyncService extends OidcUserService {

    private final UserRepository userRepository;
    private final UserInfoFactory userInfoFactory;

    @Override
    public OidcUser loadUser(OidcUserRequest req) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(req);
        AuthProvider authProvider = getAuthProvider(oidcUser.getClaims());
        OAuthUserInfo userInfo = userInfoFactory.create(authProvider, oidcUser.getAttributes());

        OAuthUserProfile userProfile = OAuthUserProfile.of(
                userInfo.getEmail(),
                userInfo.getName(),
                userInfo.getPicture(),
                userInfo.getAuthProvider());

        syncUser(userProfile);

        return new DomainAwareOidcUser(oidcUser, userInfo);
    }

    @Transactional
    public void syncUser(OAuthUserProfile profile) {
        String email = profile.email();
        if (email == null) {
            throw new OAuth2AuthenticationException("Email not provided by provider");
        }

        userRepository.findByEmail(email)
            .map(user -> user.updateFrom(profile))
            .orElseGet(() -> createNewUser(profile));
    }

    private User createNewUser(OAuthUserProfile userProfile) {
        return userRepository.save(
                User.builder()
                        .email(userProfile.email())
                        .name(userProfile.name())
                        .picture(userProfile.picture())
                        .provider(userProfile.provider())
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
