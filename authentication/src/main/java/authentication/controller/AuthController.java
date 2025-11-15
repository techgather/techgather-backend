package authentication.controller;

import authentication.config.CognitoProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CognitoProperties cognitoProperties;

    @GetMapping("/")
    public String home() {
        return """
                <h1>홈</h1>
                <a href="/oauth2/authorization/cognito">Cognito 로그인</a><br>
                <a href="/auth/me">내 정보 보기</a><br>
                <a href="/auth/logout">로그아웃</a>
                """;
    }

    @GetMapping("/me")
    public Object me(@AuthenticationPrincipal OidcUser oidcUser) {
        return oidcUser != null ? oidcUser.getClaims() : "로그인 안 됨";
    }

    @GetMapping("/token")
    public Map<String, Object> tokens(
            @RegisteredOAuth2AuthorizedClient("cognito") OAuth2AuthorizedClient client) {

        String token = client.getAccessToken().getTokenValue()
                .split("\\.")[1];
        String json = new String(Base64.getUrlDecoder().decode(token));
        String refreshToken = client.getRefreshToken() != null
                ? client.getRefreshToken().getTokenValue()
                : " No Refresh Token";

        return Map.of(
                "access_token", client.getAccessToken().getTokenValue(),
                "access_token_json", json,
                "refreshToken", refreshToken);
    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String clientId = cognitoProperties.getClientId();
        String domain = cognitoProperties.getDomainUri();  //Hosted UI Domain

        // 임시값(향후 프론트로 변경)
        String proto = Optional.ofNullable(request.getHeader("X-Forwarded-Proto"))
                .orElse(request.getScheme());

        String host = Optional.ofNullable(request.getHeader("X-Forwarded-Host"))
                .orElse(request.getServerName());

        String logoutRedirect = proto + "://" + host + "/auth/me";

        String encodedLogoutRedirect = URLEncoder.encode(logoutRedirect, StandardCharsets.UTF_8);

        String redirectUrl = String.format("%s/logout?client_id=%s&logout_uri=%s",
                domain, clientId, encodedLogoutRedirect);

        response.sendRedirect(redirectUrl);
    }
}
