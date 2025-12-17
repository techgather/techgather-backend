package domain.entity;

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
