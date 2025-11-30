package api.exception

import org.springframework.http.HttpStatus

data class ApiErrorResponse(val code: String, val message: String, val httpStatus: HttpStatus) {

    companion object {
        fun of(code: String, message: String, httpStatus: HttpStatus): ApiErrorResponse {
            return ApiErrorResponse(code, message, httpStatus)
        }
    }
}
