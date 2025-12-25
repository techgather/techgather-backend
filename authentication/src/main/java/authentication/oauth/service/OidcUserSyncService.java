package authentication.oauth.service;

import application.exception.TechGatherException;
import authentication.exception.AuthErrorCode;
import authentication.oauth.userinfo.OAuthUserInfo;
import authentication.oauth.userinfo.UserInfoFactory;
import authentication.oauth.userinfo.oidc.DomainAwareOidcUser;
import domain.entity.AuthProvider;
import domain.entity.Role;
import domain.entity.User;
import domain.repository.UserRepository;
import domain.vo.OAuthUserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static authentication.exception.AuthErrorCode.*;

@Service
@RequiredArgsConstructor
public class OidcUserSyncService extends OidcUserService {

    private final UserRepository userRepository;
    private final UserInfoFactory userInfoFactory;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest req) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(req);
        AuthProvider authProvider = getAuthProvider(oidcUser.getClaims());
        OAuthUserInfo userInfo = userInfoFactory.create(authProvider, oidcUser.getAttributes());

        OAuthUserProfile userProfile = OAuthUserProfile.of(
                userInfo.getEmail(),
                userInfo.getName(),
                userInfo.getPicture(),
                userInfo.getAuthProvider());
        Role role = extractRoleFromClaims(oidcUser.getClaims());

        syncUser(userProfile, role);

        return new DomainAwareOidcUser(oidcUser, userInfo);
    }

    public void syncUser(OAuthUserProfile profile, Role role) {
        String email = profile.email();
        if (email == null) {
            throw TechGatherException.of(EMAIL_NOT_PROVIDED);
        }

        userRepository.findByEmail(email)
            .map(user -> user.updateFrom(profile, role))
            .orElseGet(() -> createNewUser(profile, role));
    }

    private User createNewUser(OAuthUserProfile userProfile, Role role) {
        return userRepository.save(
                User.from(userProfile, role)
        );
    }

    private AuthProvider getAuthProvider(Map<String, Object> claims) {
        Object identitiesObj = claims.get("identities");

        if (!(identitiesObj instanceof List<?> list) || list.isEmpty()) {
            throw TechGatherException.of(AuthErrorCode.OIDC_IDENTITIES_NOT_PRESENT);
        }

        Object first = list.get(0);
        if (!(first instanceof Map<?, ?> identity)){
            throw TechGatherException.of(OIDC_INVALID_IDENTITY_FORMAT);
        }
        if (!(identity.get("providerName") instanceof String providerName)) {
            throw TechGatherException.of(OIDC_PROVIDER_NAME_INVALID);
        }

        return AuthProvider.from(providerName)
                .orElseThrow(() -> TechGatherException.of(AUTH_UNSUPPORTED_PROVIDER));
    }

    private Role extractRoleFromClaims(Map<String, Object> claims) {
        Object groupsObj = claims.get("cognito:groups");

        if (groupsObj instanceof List<?> groups) {
            boolean isAdmin = groups.stream()
                    .anyMatch(g -> "ADMIN".equalsIgnoreCase(String.valueOf(g)));

            if (isAdmin) {
                return Role.ADMIN;
            }
        }

        return Role.USER;
    }
}
