package authentication.controller;

import application.exception.TechGatherException;
import authentication.config.CognitoProperties;
import authentication.controller.dto.AuthResponse;
import authentication.controller.dto.RefreshRequest;
import authentication.infra.JwtKeyProvider;
import authentication.oauth.service.TokenService;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final CognitoProperties cognitoProperties;
    private final JwtKeyProvider keyProvider;
    private final TokenService tokenService;

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

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyProvider.getPublicKey())
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID("auth-key")
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey);
        return jwkSet.toJSONObject();
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest req) throws TechGatherException {
        return tokenService.refresh(req.refreshToken());
    }
}
