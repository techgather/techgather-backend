---
paths:
  - "**/*.java"
  - "**/application*.yml"
  - "**/application*.properties"
---
# Java Security

> This file extends [common/security.md](../common/security.md) with Java-specific content.

## 시크릿 관리

- 절대 소스 코드에 API 키, 토큰, 자격 증명을 하드코딩하지 않기
- 환경 변수 사용: `System.getenv("API_KEY")`
- 프로덕션 시크릿에는 시크릿 관리자 사용 (Vault, AWS Secrets Manager)
- 시크릿이 있는 로컬 설정 파일은 `.gitignore`에 포함

```java
// BAD
private static final String API_KEY = "sk-abc123...";

// GOOD — 환경 변수
String apiKey = System.getenv("PAYMENT_API_KEY");
Objects.requireNonNull(apiKey, "PAYMENT_API_KEY must be set");
```

## SQL 인젝션 방지

- 항상 파라미터화된 쿼리 사용 — 사용자 입력을 SQL에 절대 연결하지 않기
- `PreparedStatement` 또는 프레임워크의 파라미터화 쿼리 API 사용

```java
// BAD — 문자열 연결로 SQL 인젝션 위험
String sql = "SELECT * FROM orders WHERE name = '" + name + "'";

// GOOD — PreparedStatement
PreparedStatement ps = conn.prepareStatement("SELECT * FROM orders WHERE name = ?");
ps.setString(1, name);

// GOOD — JDBC 템플릿
jdbcTemplate.query("SELECT * FROM orders WHERE name = ?", mapper, name);

// GOOD — Spring Data JPA 파라미터
@Query("select o from Order o where o.name = :name")
List<Order> findByName(@Param("name") String name);
```

## 입력 검증

- 처리 전 시스템 경계에서 모든 사용자 입력 검증
- Bean Validation 사용 (`@NotNull`, `@NotBlank`, `@Size`) — DTO에 적용
- 파일 경로와 사용자 제공 문자열 새니타이즈
- 검증 실패 시 명확한 오류 메시지와 함께 거부

```java
// Spring Boot DTO 검증
public record CreateOrderRequest(
    @NotBlank @Size(max = 100) String customerName,
    @NotNull @Positive BigDecimal amount,
    @NotNull @FutureOrPresent LocalDate deliveryDate
) {}

// 컨트롤러에서 @Valid 적용
@PostMapping("/orders")
ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(orderService.create(request));
}
```

## 인증 및 인가

- 절대 커스텀 암호화 구현 금지 — 검증된 라이브러리 사용
- 비밀번호는 bcrypt 또는 Argon2로 저장, MD5/SHA1 금지
- 서비스 경계에서 인가 검사 강제
- 로그에서 민감 데이터 제거 — 비밀번호, 토큰, PII 절대 로깅 금지

## 의존성 보안

- `mvn dependency:tree` 또는 `./gradlew dependencies`로 전이 의존성 감사
- OWASP Dependency-Check 또는 Snyk으로 알려진 CVE 스캔
- 의존성 최신 상태 유지 — Dependabot 또는 Renovate 설정

## 오류 메시지

- API 응답에 스택 트레이스, 내부 경로, SQL 오류 절대 노출 금지
- 핸들러 경계에서 예외를 안전하고 일반적인 클라이언트 메시지로 매핑
- 서버 측에 상세 오류 로깅; 클라이언트에는 일반 메시지 반환

```java
@ExceptionHandler(Exception.class)
ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
    log.error("Unexpected error", ex);                    // 서버 로그에만 상세 정보
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error("Internal server error")); // 클라이언트에 일반 메시지
}
```

## 참고

skill: `springboot-security` — Spring Security 인증/인가 패턴
