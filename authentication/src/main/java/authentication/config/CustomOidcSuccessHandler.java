package authentication.config;

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
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOidcSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2AuthorizedClientService clientService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = authToken.getAuthorizedClientRegistrationId();

        // Cognito Access/Refresh Token 가져오기
        OAuth2AuthorizedClient authorizedClient = clientService.loadAuthorizedClient(
                registrationId,
                authToken.getName()
        );

        if (authorizedClient == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorized client not found");
            return;
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        OidcIdToken idToken = oidcUser.getIdToken();

        System.out.println("expires_in??? " + accessToken.getExpiresAt());
        Map<String, Object> tokenResponse = Map.of(
                "access_token", accessToken.getTokenValue(),
                "id_token", idToken.getTokenValue(),
                "refresh_token", refreshToken != null ? refreshToken.getTokenValue() : null,
                "expires_in", accessToken.getExpiresAt()
        );

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), tokenResponse);
    }
}
