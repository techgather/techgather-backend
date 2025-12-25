package authentication.controller;

import application.exception.TechGatherException;
import authentication.controller.dto.AuthTokenResponse;
import authentication.controller.dto.LogoutRequest;
import authentication.controller.dto.RefreshRequest;
import authentication.infra.JwtKeyProvider;
import authentication.oauth.service.CognitoClientHelper;
import authentication.oauth.service.TokenService;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final CognitoClientHelper cognitoClientHelper;
    private final JwtKeyProvider keyProvider;
    private final TokenService tokenService;

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        tokenService.logout(request.refreshToken());
        return ResponseEntity.ok().build();
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
    public AuthTokenResponse refresh(@RequestBody RefreshRequest req) throws TechGatherException {
        return tokenService.refresh(req.refreshToken());
    }
}
