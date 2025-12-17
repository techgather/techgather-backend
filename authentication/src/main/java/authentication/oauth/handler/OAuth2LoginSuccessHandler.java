package authentication.oauth.handler;

import authentication.controller.dto.AuthResponse;
import authentication.oauth.userinfo.OAuthUserInfo;
import authentication.oauth.userinfo.oidc.DomainAwareOidcUser;
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
 * OAuth2 / OIDC 로그인 성공 후 처리 로직을 담당하는 핸들러
 *
 * Provider별 principal을 도메인 기준 {@link OAuthUserInfo}로 변환하여
 * SecurityContext에 등록하고,
 * OAuth2 토큰 정보를 조회해 로그인 성공 응답으로 반환한다.
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

        DomainAwareOidcUser oidcUser = extractOidcUser(authentication, response);
        if (oidcUser == null) return;

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        establishDomainAuthentication(authentication, oidcUser.getOAuthUserInfo());

        OAuth2AuthorizedClient client = loadAuthorizedClient(authToken, response);
        if (client == null) return;

        request.getSession(true)
                .setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        writeAuthResponse(
                response,
                oidcUser.getOAuthUserInfo(),
                client,
                oidcUser.getIdToken().getTokenValue()
        );
    }

    private DomainAwareOidcUser extractOidcUser(Authentication authentication,
                                                HttpServletResponse response) throws IOException {

        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication");
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof DomainAwareOidcUser oidcUser)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unsupported principal");
            return null;
        }

        return oidcUser;
    }

    private OAuth2AuthorizedClient loadAuthorizedClient(
            OAuth2AuthenticationToken authToken,
            HttpServletResponse response
    ) throws IOException {

        OAuth2AuthorizedClient client =
                clientService.loadAuthorizedClient(
                        authToken.getAuthorizedClientRegistrationId(),
                        authToken.getName()
                );

        if (client == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorized client not found");
            return null;
        }

        return client;
    }

    private void establishDomainAuthentication(Authentication authentication, OAuthUserInfo domainPrincipal) {
        Authentication newAuth =
                new UsernamePasswordAuthenticationToken(
                        domainPrincipal,
                        null,
                        authentication.getAuthorities()
                );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(newAuth);
        SecurityContextHolder.setContext(context);
    }

    private void writeAuthResponse(
            HttpServletResponse response,
            OAuthUserInfo userInfo,
            OAuth2AuthorizedClient client,
            String idToken
    ) throws IOException {

        OAuth2AccessToken accessToken = client.getAccessToken();
        OAuth2RefreshToken refreshToken = client.getRefreshToken();

        AuthResponse responseDto = new AuthResponse(
                userInfo,
                accessToken.getTokenValue(),
                accessToken.getExpiresAt(),
                refreshToken != null ? refreshToken.getTokenValue() : null,
                idToken
        );

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), responseDto);
    }
}
