package authentication.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
@Getter
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true, nullable=false) private String email;
    private String name;
    private String picture;
    private String provider; // GOOGLE / APPLE / NAVER..
    private String role;     // ROLE_USER / ROLE_ADMIN ...
    private LocalDateTime lastLoginAt;
}

