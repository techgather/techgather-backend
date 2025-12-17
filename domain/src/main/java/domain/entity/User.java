package domain.entity;

import domain.vo.OAuthUserProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true, nullable=false) private String email;
    private String name;
    private String picture;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Enumerated(EnumType.STRING)
    private Role role;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    public User updateFrom(OAuthUserProfile userProfile) {
        if (userProfile.email() != null && !userProfile.name().equals(this.name)) {
            this.name = userProfile.name();
        }
        if (userProfile.picture() != null && !userProfile.picture().equals(this.picture)) {
            this.picture = userProfile.picture();
        }
        if (userProfile.provider() != null && userProfile.provider() != this.provider) {
            this.provider = userProfile.provider();
        }
        this.lastLoginAt = LocalDateTime.now();

        return this;
    }
}

