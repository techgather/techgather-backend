---
paths:
  - "**/repository/**/*.java"
---
# Repository 패턴

## 3단계 구조

복잡한 쿼리가 있는 엔티티는 3개 파일로 구성한다.

```java
// 1. JPA 인터페이스
public interface FooRepository extends JpaRepository<Foo, String>, FooRepositoryCustom {
    Optional<Foo> findByOwnerId(String ownerId);
    List<Foo> findAllByAccountId(String accountId);
}

// 2. Custom 인터페이스
public interface FooRepositoryCustom {
    List<Foo> findByCondition(String accountId, FooStatus status);
}

// 3. QueryDSL 구현체
@Repository
@RequiredArgsConstructor
public class FooRepositoryImpl implements FooRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Foo> findByCondition(String accountId, FooStatus status) {
        QFoo foo = QFoo.foo;
        return queryFactory
            .selectFrom(foo)
            .where(foo.accountId.eq(accountId), foo.status.eq(status))
            .fetch();
    }
}
```

## QueryDSL 규칙

- Q-type은 메서드 내 지역 변수로 선언: `QFoo foo = QFoo.foo`
- `.fetchOne()` — 정확히 하나 기대 (없으면 null, 둘 이상이면 예외)
- `.fetchFirst()` — 첫 번째 또는 null (유니크 보장 쿼리에 사용 금지)
- `.fetch()` — 리스트
- null-safe 집계: `.sum()`, `.count()` 뒤에 `.coalesce(0)` 체이닝

```java
QFoo foo = QFoo.foo;
QBar bar = QBar.bar;

Integer total = queryFactory
    .select(foo.amount.sum().coalesce(0))
    .from(foo)
    .join(bar).on(bar.id.eq(foo.barId))
    .where(foo.accountId.eq(accountId))
    .fetchOne();
```

## JPA vs QueryDSL 분리 기준

```java
// JPA 네이밍으로 충분
Optional<Foo> findByBarId(String barId);
List<Foo> findAllByAccountIdOrderByCreatedAtDesc(String accountId);

// QueryDSL 사용 (조인, 집계, 동적 조건)
List<Foo> findByMultipleConditions(String accountId, List<FooStatus> statuses);
```

## 금지 사항

- `FetchType.EAGER` 사용 금지
- 루프 내 지연 로딩 호출 금지 (N+1 유발)
- Repository에서 비즈니스 로직 작성 금지
