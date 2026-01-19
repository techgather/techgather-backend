package application.exception;

import lombok.Getter;

@Getter
public class UnAuthorizedException extends TechGatherException {

    public UnAuthorizedException(TechGatherErrorCode errorCode, Exception exception) {
        super(errorCode, exception);
    }
}
