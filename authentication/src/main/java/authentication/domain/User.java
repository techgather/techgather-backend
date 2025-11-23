package authentication.domain;

import authentication.userinfo.CustomOAuthUserInfo;
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

    public User updateFrom(CustomOAuthUserInfo userInfo) {
        if (userInfo.getEmail() != null && !userInfo.getName().equals(this.name)) {
            this.name = userInfo.getName();
        }
        if (userInfo.getPicture() != null && !userInfo.getPicture().equals(this.picture)) {
            this.picture = userInfo.getPicture();
        }
        if (userInfo.getAuthProvider() != null && userInfo.getAuthProvider() != this.provider) {
            this.provider = userInfo.getAuthProvider();
        }
        this.lastLoginAt = LocalDateTime.now();

        return this;
    }
}

