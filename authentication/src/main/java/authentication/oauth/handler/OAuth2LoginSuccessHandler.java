package authentication.oauth.handler;

import application.exception.TechGatherException;
import authentication.controller.dto.AuthTokenResponse;
import authentication.oauth.service.OAuth2LoginSuccessService;
import authentication.oauth.service.TokenService;
import authentication.oauth.userinfo.oidc.DomainAwareOidcUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.entity.User;
import domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static authentication.exception.AuthErrorCode.AUTH_INTERNAL_ERROR;

/**
 * OAuth2 / OIDC 로그인 성공 후 처리 로직을 담당하는 핸들러
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2LoginSuccessService oAuth2LoginSuccessService;
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        if (!(authentication instanceof OAuth2AuthenticationToken authToken)) {
            throw TechGatherException.of(AUTH_INTERNAL_ERROR);
        }

        if (!(authToken.getPrincipal() instanceof DomainAwareOidcUser oidcUser)) {
            throw TechGatherException.of(AUTH_INTERNAL_ERROR);
        }

        oAuth2LoginSuccessService.handleLoginSuccess(response, oidcUser);
    }
}
