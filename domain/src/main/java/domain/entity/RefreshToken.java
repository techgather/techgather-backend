package domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "refresh_token", unique = true, nullable = false)
    private String token;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void update(String token, LocalDateTime expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public void rotate(String newToken, long refreshTokenExpireDays) {
        this.token = newToken;
        this.expiresAt = LocalDateTime.now().plusDays(refreshTokenExpireDays);
    }

    public static RefreshToken from(String token, User user, LocalDateTime expiresAt) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(expiresAt);
        return refreshToken;
    }

    private void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    private void setUser(User user) {
        this.user = user;
    }

    private void setToken(String token) {
        this.token = token;
    }

}
