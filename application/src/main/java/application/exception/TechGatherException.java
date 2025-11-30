package application.exception;

import lombok.Getter;

@Getter
public class TechGatherException extends RuntimeException {

	private final TechGatherErrorCode errorCode;
	private final RuntimeException exception;
	private final String message;
	private final Object target;

	public TechGatherException(TechGatherErrorCode errorCode,
							   RuntimeException exception,
							   String message,
							   Object target) {
		this.errorCode = errorCode;
		this.exception = exception;
		this.message = message;
		this.target = target;
	}

	//TODO 필요한 예외에 따라 커스터마이징 필요합니다.
}
