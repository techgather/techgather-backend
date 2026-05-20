---
paths:
  - "**/dto/**/*.java"
---
# DTO 패턴

## 파일 구조

한 도메인의 모든 DTO는 `dto/FooDtos.java` 한 파일 안에 static inner record로 정의한다.

```java
public class FooDtos {

    public record CreateRequest(
        @NotBlank String title,
        Integer duration
    ) {
        public Foo toEntity(String id, String ownerId) {
            return new Foo(id, ownerId, title, duration);
        }
    }

    public record UpdateRequest(String title) {
        @AssertTrue(message = "title must not be blank")
        public boolean hasNonBlankTitleWhenPresent() {
            return title == null || !title.isBlank();
        }
    }

    public record FooResponse(
        String id,
        String title,
        FooStatus status
    ) {
        public static FooResponse from(Foo foo) {
            return new FooResponse(foo.getId(), foo.getTitle(), foo.getStatus());
        }
    }

    public record FooListResponse(
        List<FooResponse> items,
        long total,
        int page,
        int size,
        boolean hasMore
    ) {
        public static FooListResponse of(List<FooResponse> items, long total, int page, int size) {
            return new FooListResponse(items, total, page, size, (long) page * size < total);
        }
    }
}
```

## 변환 메서드 규칙

| 메서드 | 용도 |
|--------|------|
| `from(Entity)` | 단일 엔티티 → DTO |
| `fromDetail(Entity, ...)` | 연관 데이터 포함한 상세 조회 |
| `toEntity(...)` | Request DTO → 엔티티 |

## 네이밍 컨벤션

| 용도 | 이름 |
|------|------|
| 생성 요청 | `CreateRequest` |
| 수정 요청 | `UpdateRequest` |
| 단건 응답 | `FooResponse` |
| 목록 응답 | `FooListResponse` |
| 상세 응답 | `FooDetailResponse` |

## 금지 사항

- DTO에서 엔티티 import 금지 (toEntity 제외)
- record 대신 class 사용 금지
- 응답 DTO에 비즈니스 로직 작성 금지
