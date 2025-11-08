package authentication.success;

import authentication.dto.AuthResponse;
import authentication.userinfo.CustomOAuthUserInfo;
import authentication.userinfo.oidc.CustomOidcUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;

        Object principal = authentication.getPrincipal();
        CustomOAuthUserInfo userInfo = null;
        String idToken = null;

        if (principal instanceof CustomOidcUser oidcUser) {
            userInfo = oidcUser.getOAuthUserInfo();
            idToken = oidcUser.getIdToken().getTokenValue();
        }
//        else if () {  // 향후 OAUTH2.0 로그인 요청 시 최초로 한 번 추가될 코드
//        }

        OAuth2AuthorizedClient authorizedClient = clientService.loadAuthorizedClient(
                authToken.getAuthorizedClientRegistrationId(),
                authToken.getName()
        );

        if (authorizedClient == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorized client not found");
            return;
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();

        AuthResponse responseDto = new AuthResponse(
                userInfo,
                accessToken.getTokenValue(),
                accessToken.getExpiresAt(),
                refreshToken != null ? refreshToken.getTokenValue() : null,
                idToken
        );

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                userInfo,
                authentication.getCredentials(),
                authentication.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(newAuth);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", context);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), responseDto);
    }
}
