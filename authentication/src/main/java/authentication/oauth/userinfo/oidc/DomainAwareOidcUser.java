package authentication.oauth.userinfo.oidc;


import authentication.oauth.userinfo.OAuthUserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Map;

/**
 * Domain-aware OIDC Principal
 *
 * Spring Security 기본 {@link OidcUser}를 위임(delegate)하여 권한, 클레임, 토큰 정보는 유지하여,
 * 서비스 도메인에서 사용하는 {@link OAuthUserInfo}를 함께 보관한다.
 */
public class DomainAwareOidcUser implements OidcUser {
    private final OidcUser delegate;
    private final OAuthUserInfo userInfo;

    public DomainAwareOidcUser(OidcUser delegate, OAuthUserInfo userInfo) {
        this.delegate = delegate;
        this.userInfo = userInfo;
    }

    public OAuthUserInfo getOAuthUserInfo() {
        return userInfo;
    }

    @Override
    public Map<String, Object> getClaims() {
        return delegate.getClaims();
    }

    @Override
    public OidcIdToken getIdToken() {
        return delegate.getIdToken();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return delegate.getUserInfo();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
