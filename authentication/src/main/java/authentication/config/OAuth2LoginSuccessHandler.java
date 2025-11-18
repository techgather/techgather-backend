package authentication.config;

import authentication.domain.AuthProvider;
import authentication.domain.ExternalProvider;
import authentication.domain.OAuthUserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuth2/OIDC 로그인 성공 시 공통 처리 핸들러
 *
 * - provider별 사용자 정보는 ProviderHandlerResolver를 통해 통합 추출
 * - access token / (옵션) refresh token / (옵션) id token 응답
 * - Cognito/Google(OIDC) + Naver/Kakao(OAuth2) 모두 지원 (예정)
 * - 외부 Provider(Google 등)와 Auth Provider(Cognito)를 구분해 응답 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ProviderHandlerResolver resolver;
    private final OAuth2AuthorizedClientService clientService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;

        // provider별 handler를 통해 공통 사용자 정보 추출
        String registrationId = authToken.getAuthorizedClientRegistrationId();
        OAuth2ProviderHandler handler = resolver.resolve(registrationId);
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuthUserInfo userInfo = handler.extractUserInfo(oAuth2User);

        OAuth2AuthorizedClient authorizedClient = clientService.loadAuthorizedClient(
                authToken.getAuthorizedClientRegistrationId(),
                authToken.getName()
        );

        if (authorizedClient == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorized client not found");
            return;
        }

        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("user", userInfo);

        AuthProvider authProvider = AuthProvider.from(registrationId);
        if (authProvider == AuthProvider.UNKNOWN) {
            log.error("Unknown Auth Provider: {}", authProvider);
        }
        tokenResponse.put("auth_provider", authProvider.name());

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        tokenResponse.put("access_token", accessToken.getTokenValue());
        tokenResponse.put("access_token_expires_at", accessToken.getExpiresAt());

        // Cognito: refresh token은 조건부 발급이기 때문에 null 체크 필요
        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
        tokenResponse.put("refresh_token", refreshToken != null ? refreshToken.getTokenValue() : null);

        // Cognito: id token 지원
        if (oAuth2User instanceof OidcUser oidcUser) {
            String idToken = oidcUser.getIdToken().getTokenValue();
            ExternalProvider externalProvider = getExternalProvider(oidcUser.getClaims());

            tokenResponse.put("id_token", idToken);
            tokenResponse.put("external_provider", externalProvider);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), tokenResponse);
    }

    private ExternalProvider getExternalProvider(Map<String, Object> claims) {
        @SuppressWarnings("unchecked")
        Map<String, Object> identities = (Map<String, Object>) ((List) claims.get("identities")).get(0);

        String externalProvider = (String) identities.get("providerName");
        ExternalProvider provider = ExternalProvider.from(externalProvider);

        if (provider == ExternalProvider.UNKNOWN) {
            log.error("Unknown External Provider: {}", identities);
        }
        return provider;
    }
}
