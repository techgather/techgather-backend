package authentication.exception;

import application.exception.CommonClientErrorCode;
import application.exception.UnAuthorizedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnAuthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnAuthorizedException(UnAuthorizedException e) {
        String errorMessage = e.getMessage();

        CommonClientErrorCode errorCode = CommonClientErrorCode.UNAUTHORIZED;
        ApiErrorResponse response = ApiErrorResponse.of(errorCode.getCode(), errorMessage, errorCode.getHttpStatus());

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }
}
