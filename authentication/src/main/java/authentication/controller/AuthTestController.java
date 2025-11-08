package authentication.controller;

import authentication.config.CognitoProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class AuthTestController {

    private final CognitoProperties cognitoProperties;

    @GetMapping("/")
    public String home() {
        return """
                <h1>홈</h1>
                <a href="/oauth2/authorization/cognito">Cognito 로그인</a><br>
                <a href="/me">내 정보 보기</a><br>
                <a href="/logout">로그아웃</a>
                """;
    }

    @GetMapping("/me")
    public Object me(@AuthenticationPrincipal OidcUser oidcUser) {
        return oidcUser != null ? oidcUser.getClaims() : "로그인 안 됨";
    }

    @GetMapping("/logout")
    public void logout(HttpServletResponse response) throws IOException {
        String issuerUri = cognitoProperties.getIssuerUri();
        String clientId = cognitoProperties.getClientId();
        String logoutUri = URLEncoder.encode("http://localhost:8080", StandardCharsets.UTF_8);

        String redirectUrl = String.format("%s/logout?client_id=%s&logout_uri=%s",
                issuerUri, clientId, logoutUri);

        response.sendRedirect(redirectUrl);
    }
}
