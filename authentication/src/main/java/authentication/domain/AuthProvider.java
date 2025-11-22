package authentication.domain;

/**
 * 인증을 수행한 인증 제공자
 */
public enum AuthProvider {
    COGNITO,
    GOOGLE,
    UNKNOWN;

    public static AuthProvider from(String name) {
        for (AuthProvider p : values()) {
            if (p.name().equalsIgnoreCase(name)) return p;
        }
        return UNKNOWN;
    }
}
