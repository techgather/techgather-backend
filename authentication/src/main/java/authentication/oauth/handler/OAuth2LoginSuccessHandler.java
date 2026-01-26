package authentication.oauth.handler;

import authentication.dto.response.AuthResponse;
import authentication.dto.response.OAuthTokenResponse;
import authentication.oauth.userinfo.oidc.CustomOidcUser;
import authentication.service.CognitoAuthService;
import authentication.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2AuthorizedClientService clientService;
    private final ObjectMapper objectMapper;
    private final CognitoAuthService cognitoAuthService;

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

        OAuth2RefreshToken refreshToken = client.getRefreshToken();

        OAuthTokenResponse newOauthTokenResponse = cognitoAuthService.refresh(refreshToken.getTokenValue(), client.getClientRegistration().getClientId());

        // 1. Access Token → Header
        response.setHeader(
                "Authorization",
                "Bearer " + newOauthTokenResponse.accessToken()
        );

        // 2. Refresh Token → Cookie
        if (newOauthTokenResponse.refreshToken() != null) {
            CookieUtil.setRefreshTokenCookie(response, newOauthTokenResponse.refreshToken());
        }

        // 3. Response Body
        AuthResponse authResponse = AuthResponse.builder()
                .accessTokenExpiresAt(newOauthTokenResponse.accessTokenExpiresAt())
                .idToken(newOauthTokenResponse.idToken())
                .build();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), authResponse);
    }
}
