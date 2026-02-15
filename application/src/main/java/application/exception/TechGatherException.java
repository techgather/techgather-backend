package application.exception;

import lombok.Getter;

@Getter
public class TechGatherException extends RuntimeException {

    private final TechGatherErrorCode errorCode;
    private final Exception exception;
    private final String message;
    private final Object target;

    public TechGatherException(TechGatherErrorCode errorCode, Exception exception) {
        this.errorCode = errorCode;
        this.exception = exception;
        this.target = null;
        this.message = errorCode.getMessage();
    }

    public TechGatherException(TechGatherErrorCode errorCode) {
        this.errorCode = errorCode;
        this.exception = null;
        this.target = null;
        this.message = errorCode.getMessage();
    }
}
