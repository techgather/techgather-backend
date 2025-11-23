package authentication.controller;

import authentication.config.CognitoProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CognitoProperties cognitoProperties;

    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String clientId = cognitoProperties.getClientId();
        String domain = cognitoProperties.getDomainUri();  //Hosted UI Domain

        // 임시값(향후 프론트로 변경)
        String proto = Optional.ofNullable(request.getHeader("X-Forwarded-Proto"))
                .orElse(request.getScheme());

        String host = Optional.ofNullable(request.getHeader("X-Forwarded-Host"))
                .orElse(request.getServerName());

        String logoutRedirect = proto + "://" + host + "/auth";

        String encodedLogoutRedirect = URLEncoder.encode(logoutRedirect, StandardCharsets.UTF_8);

        String redirectUrl = String.format("%s/logout?client_id=%s&logout_uri=%s",
                domain, clientId, encodedLogoutRedirect);

        response.sendRedirect(redirectUrl);
    }
}
