package authentication.exception;

import application.exception.TechGatherErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements TechGatherErrorCode {
    USER_NOT_FOUND(
            "USER_NOT_FOUND",
            "존재하지 않는 사용자입니다.",
            HttpStatus.NOT_FOUND
    ),
    EMAIL_NOT_PROVIDED(
            "EMAIL_NOT_PROVIDED",
            "이메일 정보가 제공되지 않았습니다.",
            HttpStatus.BAD_REQUEST
    ),
    REFRESH_TOKEN_NOT_FOUND(
            "REFRESH_TOKEN_NOT_FOUND",
            "",
            HttpStatus.UNAUTHORIZED
    ),
    REFRESH_TOKEN_EXPIRED(
            "REFRESH_TOKEN_EXPIRED",
            "유효한 리프레시 토큰이 존재하지 않습니다.",
            HttpStatus.UNAUTHORIZED
    ),
    REFRESH_TOKEN_REVOKED(
            "REFRESH_TOKEN_REVOKED",
            "리프레시 토큰이 만료되었습니다.",
            HttpStatus.UNAUTHORIZED
    ),
    LOCAL_JWT_KEY_GENERATION_FAILED(
            "LOCAL_JWT_KEY_GENERATION_FAILED",
            "서버 내부 키 생성에 실패했습니다.",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),
    OIDC_IDENTITIES_NOT_PRESENT(
            "OIDC_IDENTITIES_NOT_PRESENT",
            "OIDC identities 정보가 존재하지 않아 소셜 로그인 제공자를 확인할 수 없습니다.",
            HttpStatus.BAD_REQUEST
    ),
    OIDC_INVALID_IDENTITY_FORMAT(
            "OIDC_INVALID_IDENTITY_FORMAT",
            "OIDC 사용자 정보 형식이 올바르지 않습니다.",
            HttpStatus.BAD_REQUEST
    ),
    AUTH_UNSUPPORTED_PROVIDER(
            "AUTH_UNSUPPORTED_PROVIDER",
            "지원하지 않는 인증 제공자입니다.",
            HttpStatus.BAD_REQUEST
    ),
    OIDC_PROVIDER_NAME_INVALID(
            "OIDC_PROVIDER_NAME_INVALID",
            "OIDC providerName이 없거나 형식이 올바르지 않습니다.",
            HttpStatus.BAD_REQUEST
    ),
    AUTH_INTERNAL_ERROR(
            "AUTH_INTERNAL_ERROR",
            "인증 처리 과정에서 내부 오류가 발생했습니다.",
            HttpStatus.INTERNAL_SERVER_ERROR
    );;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
