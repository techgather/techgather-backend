package domain.vo;

import domain.entity.AuthProvider;

public record OAuthUserProfile(
        String email,
        String name,
        String picture,
        AuthProvider provider
) {
    public static OAuthUserProfile of(String email, String name, String picture, AuthProvider provider) {
        return new OAuthUserProfile(email, name, picture, provider);}
}
