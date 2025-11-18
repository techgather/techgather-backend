package authentication.domain;

public record OAuthUserInfo(
        String email,
        String name,
        String picture
) {}