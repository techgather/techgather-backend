package domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "provider_refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProviderRefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Column(name = "provider_refresh_token", nullable = false, length = 4096)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "refresh_token_id", nullable = false, unique = true)
    private RefreshToken refreshToken;

    public static ProviderRefreshToken from(AuthProvider provider,
                                            String token,
                                            LocalDateTime expiresAt,
                                            RefreshToken refreshToken) {
        ProviderRefreshToken providerRefreshToken = new ProviderRefreshToken();
        providerRefreshToken.provider = provider;
        providerRefreshToken.token = token;
        providerRefreshToken.expiresAt = expiresAt;
        providerRefreshToken.refreshToken = refreshToken;
        return providerRefreshToken;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
