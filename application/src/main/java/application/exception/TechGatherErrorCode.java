package application.exception;

import org.springframework.http.HttpStatus;

public interface TechGatherErrorCode {

	String name();

	String getCode();

	String getMessage();

	HttpStatus getHttpStatus();
}
