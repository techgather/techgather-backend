package authentication.oauth.handler;

import authentication.controller.dto.AuthResponse;
import authentication.controller.dto.UserProfileDto;
import authentication.oauth.service.TokenService;
import authentication.oauth.userinfo.OAuthUserInfo;
import authentication.oauth.userinfo.oidc.DomainAwareOidcUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.entity.User;
import domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof DomainAwareOidcUser oidcUser)) {
            return;
        }

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient client = loadAuthorizedClient(authToken, response);
        if (client == null) return;

        writeAuthResponse(
                response,
                oidcUser.getOAuthUserInfo(),
                authentication
        );
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

    private void writeAuthResponse(
            HttpServletResponse response,
            OAuthUserInfo userInfo,
            Authentication authentication) throws IOException {

        DomainAwareOidcUser oidcUser = (DomainAwareOidcUser) authentication.getPrincipal();

        User user = userRepository.findByEmail(oidcUser.getOAuthUserInfo().getEmail())
                .orElseThrow();

        String accessToken = tokenService.issueAccessToken(user);
        String refreshToken = tokenService.issueRefreshToken(user);

        AuthResponse responseDto = new AuthResponse(
                user.getId(),
                UserProfileDto.from(user),
                accessToken,
                refreshToken,
                user.getRole()
        );

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), responseDto);
    }
}
