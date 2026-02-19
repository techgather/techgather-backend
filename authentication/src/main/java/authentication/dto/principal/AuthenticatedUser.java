package authentication.dto.principal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AuthenticatedUser(
        String sub,
        String username,
        @JsonProperty("cognito:groups")
        List<String> cognitoGroups
) {
}
