package authentication.userinfo.oidc;


import authentication.userinfo.CustomOAuthUserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Map;

/**
 * Spring Security 기본 OidcUser에 서비스 공통 사용자 정보(OAuthUserInfo)를 추가한 인증 Principal
 *
 * - 원본 OidcUser(delegate)의 권한/클레임/토큰 정보는 그대로 유지
 * - Provider별 attributes가 저장된 OAuthUserInfo를 함께 보관
 *
 */

public class CustomOidcUser implements OidcUser {
    private final OidcUser delegate;        // 원본 객체
    private final CustomOAuthUserInfo userInfo;   // sucessHandler에서도 사용할 추가 정보

    public CustomOidcUser(OidcUser delegate, CustomOAuthUserInfo userInfo) {
        this.delegate = delegate;
        this.userInfo = userInfo;
    }

    public CustomOAuthUserInfo getOAuthUserInfo() {
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
