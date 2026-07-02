---
paths:
  - "**/entity/**/*.java"
  - "**/domain/**/*.java"
---
# Entity 패턴

## 클래스 구조

```java
@Entity
@Table(name = "post", indexes = {
    @Index(name = "idx_post_status_lang", columnList = "status, language")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTime {

    @Id
    private Long postId;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    public static Post create(Long postId, String title, ...) {
        Post post = new Post();
        post.postId = postId;
        post.status = PostStatus.NOT_PUBLISHED;
        return post;
    }
}
```

## ID

- 타입: `Long` (Snowflake)
- 생성: `SnowFlake.getInstance().nextId()` — `application.generator.SnowFlake`
- DB auto-increment 사용 금지
- `@GeneratedValue` 사용 금지 — 서비스/팩토리에서 생성 후 주입

## Enum 컬럼

```java
@Enumerated(EnumType.STRING)
@Column(length = 30, nullable = false)
private FooStatus status = FooStatus.ACTIVE;
```

- `EnumType.STRING` 필수 — `ORDINAL` 사용 금지
- `length = 30` 설정 (값 추가 시 마이그레이션 불필요)

## 기본값 처리

nullable 컬럼 기본값은 `@PrePersist`에서 처리한다.

```java
@PrePersist
void applyDefaults() {
    if (content == null) content = "";
    if (count == null) count = 0;
}
```

## 연관관계

`@ManyToOne`, `@OneToMany` 매핑 대신 **ID 참조**를 선호한다.
조인이 필요하면 Repository(QueryDSL)에서 명시적으로 처리한다.

```java
// 권장
private String ownerId;

// 지양 (필요한 경우에만 사용)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "owner_id")
private Owner owner;
```

## 금지 사항

- `FetchType.EAGER` 사용 금지
- 엔티티에서 DTO import 금지
- 비즈니스 로직을 엔티티에 과도하게 집중시키지 않음 (상태 전이 메서드는 허용)
