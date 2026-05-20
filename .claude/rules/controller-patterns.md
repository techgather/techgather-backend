---
paths:
  - "**/controller/**/*.java"
  - "**/web/**/*.java"
---
# Controller 패턴

## 클래스 구조

```java
@RestController
@RequestMapping("/api/v1/{domain}")
@RequiredArgsConstructor
@Validated
public class FooController {
    private final FooService fooService;
}
```

## 응답 포맷

공통 래퍼 없이 DTO 직접 반환.

```java
// 조회
public FooDtos.FooResponse getFoo(...) { ... }

// 생성
@ResponseStatus(HttpStatus.CREATED)
public FooDtos.FooResponse createFoo(...) { ... }

// 삭제 / 본문 없는 수정
@ResponseStatus(HttpStatus.NO_CONTENT)
public void deleteFoo(...) { ... }
```

## Validation

- `@Validated` 클래스 레벨 선언
- Request record 필드: `@NotBlank`, `@NotNull`, `@Positive`, `@PositiveOrZero`
- nullable 필드 조건부 검증: `@AssertTrue` 메서드로 처리

```java
public record FooUpdateRequest(String title) {
    @AssertTrue(message = "title must not be blank")
    public boolean hasNonBlankTitleWhenPresent() {
        return title == null || !title.isBlank();
    }
}
```

## 페이지네이션 파라미터

```java
@RequestParam(defaultValue = "1") @Positive int page,
@RequestParam(defaultValue = "20") @Positive int size
```

## 금지 사항

- 컨트롤러에 비즈니스 로직 작성 금지
- `@Autowired` 필드 주입 금지
- 엔티티 직접 반환 금지
