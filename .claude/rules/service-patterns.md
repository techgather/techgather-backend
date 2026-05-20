---
paths:
  - "**/service/**/*.java"
---
# Service 레이어 패턴

## 클래스 구조

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class FooService {
    private final FooRepository fooRepository;
}
```

모든 의존성은 `@RequiredArgsConstructor` + `private final`. `@Autowired` 사용 금지.

## @Transactional

- `org.springframework.transaction.annotation.Transactional` 만 사용 — `jakarta.transaction.Transactional` 금지
- SELECT만 수행하는 메서드: `@Transactional(readOnly = true)` 필수
- INSERT/UPDATE/DELETE 또는 다른 `@Transactional` 메서드 호출: `@Transactional`

```java
@Transactional(readOnly = true)
public FooDtos.FooResponse getFoo(String id) { ... }

@Transactional
public FooDtos.FooResponse createFoo(String ownerId, FooDtos.CreateRequest request) { ... }
```

## 조회 + 소유권 체크 패턴

엔티티 조회 후 소유권 검증이 반복되면 private 메서드로 분리한다.

```java
private Foo findOwnedFoo(String ownerId, String fooId) {
    Foo foo = fooRepository.findById(fooId)
        .orElseThrow(() -> new NotFoundException("리소스를 찾을 수 없습니다."));
    if (!foo.getOwnerId().equals(ownerId)) {
        throw new ForbiddenException("접근 권한이 없습니다.");
    }
    return foo;
}
```

## Service 분리 기준

단일 Service가 커질 때:
- **파이프라인 단계별**: `FooService` → `FooProcessingService`, `FooQueryService`
- **외부 서비스별**: `AuthService` → `AppleAuthService`, `KakaoAuthService`
- **내부 호출 전용**: `FooInternalService` — Kafka 컨슈머 등에서만 호출

## 입력 정규화

null 또는 유효하지 않은 값은 서비스 진입점에서 정규화한다.

```java
private int normalizeDuration(Integer duration) {
    return (duration == null || duration < 0) ? 0 : duration;
}
```

## 금지 사항

- `@Transactional(readOnly = true)` 메서드에서 엔티티 변경 금지
- Service 간 순환 의존 금지
- HTTP 관련 객체(HttpServletRequest 등) Service에서 직접 사용 금지
