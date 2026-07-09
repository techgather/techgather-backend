package authentication.oauth.userinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import domain.constants.AuthProvider;
import domain.constants.Role;
import domain.entity.User;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public interface CustomOAuthUserInfo {
    String getSubject();
    String getEmail();
    String getName();
    String getPicture();
    @JsonProperty("auth_provider")
    AuthProvider getAuthProvider();

    // 신규 엔티티 생성 로직
    default User toEntity() {
        return User.builder()
                .id(getSubject())
                .email(getEmail())
                .name(getName())
                .picture(getPicture())
                .provider(getAuthProvider())
                .role(Role.USER) // 기본 권한
                .createdAt(LocalDateTime.now())
                .build();
    }

    // 기존 엔티티 업데이트 로직
    default User updateEntity(User user) {
        if (StringUtils.hasText(getName())) {
            user.setName(getName());
        }
//        if (getPicture() != null) {
//            user.setPicture(getPicture());
//        }
//        if (getEmail() != null && !getEmail().equals(user.getEmail())) {
//            user.setEmail(getEmail());
//        }
//        if (targetRole != null) {
//            user.setRole(targetRole);
//        }
        return user;
    }
}
