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

import static authentication.exception.AuthErrorCode.AUTH_INTERNAL_ERROR;

@Service
@RequiredArgsConstructor
public class OAuth2LoginSuccessService {
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public void handleLoginSuccess(
            HttpServletResponse response,
            DomainAwareOidcUser oidcUser) throws IOException {

        String email = oidcUser.getOAuthUserInfo().getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> TechGatherException.of(AUTH_INTERNAL_ERROR));

        String accessToken = tokenService.issueAccessToken(user);
        String refreshToken = tokenService.issueRefreshToken(user);
        AuthTokenResponse responseDto = AuthTokenResponse.from(user, accessToken, refreshToken);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), responseDto);
    }
}
