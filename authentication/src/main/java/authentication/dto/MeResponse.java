package authentication.dto;

import authentication.domain.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MeResponse {
    private final String name;
    private final String email;
    private final String picture;
    private final AuthProvider provider;
}
