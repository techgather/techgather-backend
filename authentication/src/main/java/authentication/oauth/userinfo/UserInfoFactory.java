package authentication.oauth.userinfo;

import domain.entity.AuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * OAuth Provider별 사용자 정보 생성을 담당하는 Factory
 *
 * 전달받은 AuthProvider에 대응하는 OAuthUserInfoCreator를 통해
 * Provider 독립적인 사용자 정보를 생성한다.
 */
@Component
@RequiredArgsConstructor
public class UserInfoFactory {
    private final List<OAuthUserInfoCreator> creators;

    public OAuthUserInfo create(AuthProvider provider, Map<String, Object> attributes) {
        return creators.stream()
                .filter(c -> c.supports(provider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported provider: " + provider))
                .create(attributes);
    }
}
