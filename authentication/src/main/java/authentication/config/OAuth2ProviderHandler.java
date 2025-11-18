package authentication.config;

import authentication.domain.OAuthUserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2ProviderHandler {
    // 어떤 provider를 처리하는지 판별
    boolean supports(String registrationId);

    // provider별 유저 정보 구조를 공통 모델로 변환
    OAuthUserInfo extractUserInfo(OAuth2User oAuth2User);
}
