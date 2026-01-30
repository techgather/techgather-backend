package domain.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER, ADMIN;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }

    public static Role fromAuthority(String authority) {
        if (!StringUtils.hasText(authority)) return null;

        String roleName = authority.replace("ROLE_", "");
        return Role.valueOf(roleName);
    }
}
