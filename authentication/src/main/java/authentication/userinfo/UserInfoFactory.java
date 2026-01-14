package authentication.userinfo;

import authentication.domain.AuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
