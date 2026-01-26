package authentication.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiErrorResponse {
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    private ApiErrorResponse(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public static ApiErrorResponse of(String code, String message, HttpStatus httpStatus) {
        return new ApiErrorResponse(code, message, httpStatus);
    }
}
