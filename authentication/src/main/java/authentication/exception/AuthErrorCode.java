package authentication.exception;

import application.exception.TechGatherErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements TechGatherErrorCode {
    USER_NOT_FOUND(
            "USER_NOT_FOUND",
            "",
            HttpStatus.NOT_FOUND
    ),
    EMAIL_NOT_PROVIDED(
            "EMAIL_NOT_PROVIDED",
            "",
            HttpStatus.BAD_REQUEST
    ),
    REFRESH_TOKEN_NOT_FOUND(
            "REFRESH_TOKEN_NOT_FOUND",
            "",
            HttpStatus.UNAUTHORIZED
    ),
    REFRESH_TOKEN_EXPIRED(
            "REFRESH_TOKEN_EXPIRED",
            "",
            HttpStatus.UNAUTHORIZED
    ),
    REFRESH_TOKEN_REVOKED(
            "REFRESH_TOKEN_REVOKED",
            "",
            HttpStatus.UNAUTHORIZED
    ),
    LOCAL_JWT_KEY_GENERATION_FAILED(
            "LOCAL_JWT_KEY_GENERATION_FAILED",
            "",
            HttpStatus.INTERNAL_SERVER_ERROR
    );

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
