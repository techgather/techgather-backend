package domain.entity;

import java.util.Arrays;
import java.util.Optional;

public enum AuthProvider {
    GOOGLE;

    public static Optional<AuthProvider> from(String name) {
        return Arrays.stream(values())
                .filter(p -> p.name().equalsIgnoreCase(name))
                .findFirst();
    }
}
