---
paths:
  - "**/*.java"
---
# 에러 처리 패턴

## 예외 클래스 구조

도메인 예외는 HTTP 상태코드 기반으로 분류하고 `@RestControllerAdvice`에서 중앙 처리한다.

```java
// 사용할 예외 클래스 (프로젝트에 맞게 정의)
throw new BadRequestException("잘못된 요청입니다.");
throw new NotFoundException("리소스를 찾을 수 없습니다.");
throw new ForbiddenException("접근 권한이 없습니다.");
throw new ConflictException("이미 존재합니다.");
throw new UnauthorizedException("인증이 필요합니다.");
```

## GlobalExceptionHandler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of(404, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ErrorResponse.of(400, message));
    }
}
```

## 에러 응답 포맷

```json
{
  "status": 404,
  "code": "NOT_FOUND",
  "message": "리소스를 찾을 수 없습니다."
}
```

## 규칙

- 비즈니스 예외는 반드시 커스텀 예외 클래스 사용 — `RuntimeException` 직접 throw 금지
- catch 블록에서 예외 무시(swallow) 금지 — 반드시 로깅하거나 재throw
- 예외 메시지에 민감한 정보(비밀번호, 토큰 등) 포함 금지
