package authentication.oauth.service;

import application.exception.TechGatherException;
import authentication.controller.dto.AuthTokenResponse;
import authentication.infra.JwtKeyProvider;
import domain.entity.AuthProvider;
import domain.entity.ProviderRefreshToken;
import domain.entity.RefreshToken;
import domain.entity.User;
import domain.repository.ProviderRefreshTokenRepository;
import domain.repository.RefreshTokenRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import static authentication.exception.AuthErrorCode.AUTH_INTERNAL_ERROR;
import static authentication.exception.AuthErrorCode.REFRESH_TOKEN_INVALID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtKeyProvider jwtKeyProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ProviderRefreshTokenRepository providerRefreshTokenRepository;
    private final CognitoService cognitoService;

    private static final long REFRESH_TOKEN_EXPIRE_DAYS = 30;
    private static final long ACCESS_TOKEN_EXPIRE_SECONDS = 60 * 15;

    public String issueAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("provider", user.getProvider().name())
                .setIssuer("auth.techgather.io")
                .setIssuedAt(new Date())
                .setExpiration(
                        Date.from(Instant.now().plusSeconds(ACCESS_TOKEN_EXPIRE_SECONDS))
                )
                .signWith(jwtKeyProvider.getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    @Transactional
    public String issueRefreshToken(User user) {
        String uuid = UUID.randomUUID().toString();

        refreshTokenRepository
                .findByUser(user)
                .ifPresentOrElse(v -> v.update(
                        uuid,
                        LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRE_DAYS)
                ), () -> {
                    RefreshToken refreshToken = RefreshToken.from(uuid, user, LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRE_DAYS));
                    refreshTokenRepository.save(refreshToken);
                });

        return uuid;
    }

    @Transactional
    public AuthTokenResponse refresh(String refreshTokenValue) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> TechGatherException.of(REFRESH_TOKEN_INVALID));

        if (refreshToken.isExpired()) {
            throw TechGatherException.of(REFRESH_TOKEN_INVALID);
        }

        User user = refreshToken.getUser();

        String accessToken = issueAccessToken(user);
        String newRefreshToken = UUID.randomUUID().toString();
        refreshToken.rotate(newRefreshToken, REFRESH_TOKEN_EXPIRE_DAYS);

        return AuthTokenResponse.from(user, accessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() -> TechGatherException.of(REFRESH_TOKEN_INVALID));

        providerRefreshTokenRepository.findByRefreshToken(token)
                .ifPresent(providerToken -> {
                    cognitoService.revokeRefreshToken(providerToken.getToken());
                    providerRefreshTokenRepository.delete(providerToken);
                });

        refreshTokenRepository.delete(token);
    }

    @Transactional
    public void saveProviderToken(AuthProvider provider, String authRefreshToken, String providerRefreshToken, LocalDateTime providerRefreshTokenExpiresAt) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(authRefreshToken)
                .orElseThrow(() -> TechGatherException.of(AUTH_INTERNAL_ERROR));

        ProviderRefreshToken providerToken =
                providerRefreshTokenRepository.findByRefreshToken(refreshToken)
                        .orElse(null);

        if (providerToken == null) {
            save(provider, providerRefreshToken, providerRefreshTokenExpiresAt, refreshToken);
            return;
        }

        if (providerToken.isExpired()) {
            providerRefreshTokenRepository.delete(providerToken);
            save(provider, providerRefreshToken, providerRefreshTokenExpiresAt, refreshToken);
        }
    }

    private void save(AuthProvider provider, String token, LocalDateTime expiresAt, RefreshToken refreshToken) {
        providerRefreshTokenRepository.save(
                ProviderRefreshToken.from(provider, token, expiresAt, refreshToken)
        );
    }
}
