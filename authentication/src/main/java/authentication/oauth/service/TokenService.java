package authentication.oauth.service;

import application.exception.TechGatherException;
import authentication.controller.dto.AuthTokenResponse;
import authentication.infra.JwtKeyProvider;
import domain.entity.RefreshToken;
import domain.entity.User;
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

import static authentication.exception.AuthErrorCode.REFRESH_TOKEN_EXPIRED;
import static authentication.exception.AuthErrorCode.REFRESH_TOKEN_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtKeyProvider jwtKeyProvider;
    private final RefreshTokenRepository refreshTokenRepository;

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

        RefreshToken refreshToken = refreshTokenRepository
                .findByUser(user)
                .orElseGet(() -> RefreshToken.builder()
                        .user(user)
                        .build()
                );

        refreshToken.update(
                uuid,
                LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRE_DAYS)
        );

        refreshTokenRepository.save(refreshToken);
        return uuid;
    }

    @Transactional
    public AuthTokenResponse refresh(String refreshTokenValue) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> TechGatherException.of(REFRESH_TOKEN_NOT_FOUND));

        if (refreshToken.isExpired()) {
            throw TechGatherException.of(REFRESH_TOKEN_EXPIRED);
        }

        User user = refreshToken.getUser();

        String accessToken = issueAccessToken(user);
        String newRefreshToken = UUID.randomUUID().toString();
        refreshToken.rotate(newRefreshToken, REFRESH_TOKEN_EXPIRE_DAYS);

        return AuthTokenResponse.from(user, accessToken, newRefreshToken);
    }
}
