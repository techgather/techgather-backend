package application.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    USER("USER", "일반 클라이언트 등급"),
    ADMIN("ADMIN", "어드민 등급");

    private final String code;
    private final String description;
}
