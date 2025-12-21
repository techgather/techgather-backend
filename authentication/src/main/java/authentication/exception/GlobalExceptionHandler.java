package authentication.exception;

import application.exception.TechGatherErrorCode;
import application.exception.TechGatherException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TechGatherException.class)
    public ResponseEntity<AuthErrorResponse> handleTechGatherException(TechGatherException e) {
        TechGatherErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new AuthErrorResponse(errorCode.getCode(), errorCode.getMessage()));
    }
}