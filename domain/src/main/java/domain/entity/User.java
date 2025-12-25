package domain.entity;

import domain.common.BaseTime;
import domain.vo.OAuthUserProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true, nullable=false)
    private String email;

    private String name;
    private String picture;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    public static User from(OAuthUserProfile userProfile, Role role) {
        User user = new User();
        user.role = role;
        user.email = userProfile.email();
        user.provider = userProfile.provider();
        user.name = userProfile.name();
        user.picture = userProfile.picture();
        user.lastLoginAt = LocalDateTime.now();
        return user;
    }

    public User updateFrom(OAuthUserProfile userProfile, Role role) {
        if (userProfile.name() != null && !userProfile.name().equals(this.name)) {
            this.name = userProfile.name();
        }
        if (userProfile.picture() != null && !userProfile.picture().equals(this.picture)) {
            this.picture = userProfile.picture();
        }
        if (userProfile.provider() != null && userProfile.provider() != this.provider) {
            this.provider = userProfile.provider();
        }
        if (role != null && this.role != role) {
            this.role = role;
        }

        this.lastLoginAt = LocalDateTime.now();

        return this;
    }
}

