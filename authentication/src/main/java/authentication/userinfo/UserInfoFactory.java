package authentication.userinfo;

import authentication.domain.AuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Provider별 사용자 정보 생성 전략(OAuthUserInfoCreator)을 관리하는 Factory
 *
 * - 전달받은 AuthProvider에 대응하는 Creator를 찾아 OAuthUserInfo를 생성
 * - Provider별 attribute 파싱 로직을 외부로 분리하여 if/else 없이 확장
 * - 신규 Provider 추가 시 Creator만 구현해서 Bean으로 등록하면 자동 적용됨
 *
 * 인증 로직이 Provider 종류에 의존하지 않도록 UserInfo 생성 책임을 캡슐화
 *
 */
@Component
@RequiredArgsConstructor
public class UserInfoFactory {
    private final List<CustomOAuthUserInfoCreator> creators;

    public CustomOAuthUserInfo create(AuthProvider provider, Map<String, Object> attributes) {
        return creators.stream()
                .filter(c -> c.supports(provider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported provider: " + provider))
                .create(attributes);
    }
}
