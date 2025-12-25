package authentication.oauth.service;

import application.exception.TechGatherException;
import authentication.controller.dto.AuthTokenResponse;
import authentication.oauth.userinfo.oidc.DomainAwareOidcUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.entity.User;
import domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

import static authentication.exception.AuthErrorCode.AUTH_INTERNAL_ERROR;

@Service
@RequiredArgsConstructor
public class OAuth2LoginSuccessService {
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public void handleLoginSuccess(
            HttpServletResponse response,
            DomainAwareOidcUser oidcUser,
            String providerRefreshToken,
            LocalDateTime providerRefreshTokenExpiresAt) throws IOException {

        String email = oidcUser.getOAuthUserInfo().getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> TechGatherException.of(AUTH_INTERNAL_ERROR));

        String authAccessToken = tokenService.issueAccessToken(user);
        String authRefreshToken = tokenService.issueRefreshToken(user);

        AuthTokenResponse responseDto = AuthTokenResponse
                .from(user, authAccessToken, authRefreshToken);

        tokenService.saveProviderToken(user.getProvider(), authRefreshToken, providerRefreshToken, providerRefreshTokenExpiresAt);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), responseDto);
    }
}
