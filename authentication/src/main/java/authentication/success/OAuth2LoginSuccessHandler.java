package authentication.success;

import authentication.dto.AuthResponse;
import authentication.userinfo.CustomOAuthUserInfo;
import authentication.userinfo.oidc.CustomOidcUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2/OIDC 로그인 성공 이후 클라이언트에 반환할 인증 응답을 생성하는 SuccessHandler
 *
 * - Principal(CustomOidcUser)에서 provider와 무관한 공통 사용자 정보(OAuthUserInfo) 추출
 * - Access Token / (조건부) Refresh Token / (조건부) ID Token을 조합해 응답 DTO 생성
 *
 * 현재는 OIDC 기반(Cognito 소셜 계정 포함)에 맞춰 구현되어 있으며,
 * 향후 순수 OAuth2 로그인 처리 시 분기만 추가하여 확장 예정
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2AuthorizedClientService clientService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token =
                (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient(
                        token.getAuthorizedClientRegistrationId(),
                        token.getName()
                );

        OAuth2AccessToken accessToken = client.getAccessToken();
        OAuth2RefreshToken refreshToken = client.getRefreshToken();

        CustomOidcUser principal =
                (CustomOidcUser) authentication.getPrincipal();

        String idToken = principal.getIdToken().getTokenValue();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken.getTokenValue())
                .accessTokenExpiresAt(accessToken.getExpiresAt())
                .refreshToken(refreshToken != null ? refreshToken.getTokenValue() : null)
                .idToken(idToken)
                .build();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        objectMapper.writeValue(response.getWriter(), authResponse);
    }
}
