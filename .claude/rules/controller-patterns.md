---
paths:
  - "**/controller/**/*.java"
  - "**/controller/**/*.kt"
---
# Controller 패턴

api 모듈은 Kotlin, authentication 모듈은 Java. 각 언어에 맞는 스타일 사용.

## 클래스 구조 (Kotlin — api 모듈)

```kotlin
@RestController
@RequestMapping("/api/foos")
@Tag(name = "1-1 Foos", description = "Foo APIs")
class FooController(
    private val fooService: FooService
) {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Foo 목록 조회", operationId = "u1-foo-list")
    fun getFoos(
        @RequestParam(required = false) language: Language?
    ): List<FooResponse> {
        return fooService.getFoos(language)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Foo 생성", operationId = "a1-foo-create")
    fun createFoo(
        @Valid @RequestBody request: CreateFooRequest
    ): FooResponse {
        return fooService.createFoo(request)
    }
}
```

## 응답 포맷

공통 래퍼 없이 DTO 직접 반환. `@ResponseStatus`로 상태코드 명시.

```kotlin
// 조회 — 200 OK (기본값)
@ResponseStatus(HttpStatus.OK)
fun getFoo(...): FooResponse

// 생성 — 201 Created
@ResponseStatus(HttpStatus.CREATED)
fun createFoo(...): FooResponse

// 삭제 / 본문 없는 수정 — 204 No Content
@ResponseStatus(HttpStatus.NO_CONTENT)
fun deleteFoo(...)
```

## 커서 페이지네이션 파라미터

```kotlin
@RequestParam(required = false) lastPostId: String?,
@RequestParam(defaultValue = "20") limit: Long
```

## Swagger 어노테이션

- `@Tag(name = "N-M Domain")` — 컨트롤러 레벨
- `@Operation(summary = "...", operationId = "u1-xxx")` — 메서드 레벨
- operationId 네이밍: `u` = user API, `a` = admin API + 순번 + 도메인

## 금지 사항

- 컨트롤러에 비즈니스 로직 작성 금지
- `@Autowired` 필드 주입 금지
- 엔티티 직접 반환 금지
