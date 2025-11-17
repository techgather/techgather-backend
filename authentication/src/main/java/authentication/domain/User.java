package authentication.domain;

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
    private Provider provider;

    @Enumerated(EnumType.STRING)
    private Role role;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    public void updateLastLogin(LocalDateTime now) {
        this.lastLoginAt = now;
    }
}

