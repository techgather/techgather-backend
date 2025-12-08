package api.exception

import application.exception.CommonServerErrorCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val errorMessage = e.bindingResult.fieldErrors
            .mapNotNull { it.defaultMessage }
            .joinToString(", ")
        val errorCode = CommonServerErrorCode.VALID_EXCEPTION
        val response = ApiErrorResponse.of(errorCode.code, errorMessage, errorCode.httpStatus)
        return ResponseEntity.status(errorCode.httpStatus).body(response)
    }

}