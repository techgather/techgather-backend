package application.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonServerErrorCode implements TechGatherErrorCode {

	VALID_EXCEPTION("VALID_EXCEPTION", "", HttpStatus.BAD_REQUEST);


	private final String code;
	private final String message;
	private final HttpStatus httpStatus;

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
}
