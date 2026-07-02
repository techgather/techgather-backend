---
paths:
  - "**/*.java"
---
# Java Coding Style

> This file extends [common/coding-style.md](../common/coding-style.md) with Java-specific content.

## 포매팅

- **google-java-format** 또는 **Checkstyle** (Google 또는 Sun 스타일) 강제 적용
- 파일당 하나의 공개 최상위 타입
- 일관된 들여쓰기: 2 또는 4 스페이스 (프로젝트 표준에 맞춤)
- 멤버 순서: 상수, 필드, 생성자, 공개 메서드, protected, private

## 불변성

- 값 타입에는 `record` 선호 (Java 16+)
- 기본적으로 필드를 `final`로 표시 — 필요한 경우에만 가변 상태 사용
- 공개 API에서 방어적 복사 반환: `List.copyOf()`, `Map.copyOf()`, `Set.copyOf()`

```java
// GOOD — 불변 값 타입
public record OrderSummary(Long id, String customerName, BigDecimal total) {}

// GOOD — final 필드, setter 없음
public class Order {
    private final Long id;
    private final List<LineItem> items;

    public List<LineItem> getItems() {
        return List.copyOf(items);
    }
}
```

## 명명 규칙

표준 Java 관례 준수:
- `PascalCase` — 클래스, 인터페이스, 레코드, 열거형
- `camelCase` — 메서드, 필드, 매개변수, 지역 변수
- `SCREAMING_SNAKE_CASE` — `static final` 상수
- 패키지: 모두 소문자, 역방향 도메인 (`com.example.app.service`)

## 모던 Java 기능

명확성을 향상시키는 경우 모던 언어 기능 사용:
- **Records** — DTO 및 값 타입 (Java 16+)
- **Sealed classes** — 닫힌 타입 계층 (Java 17+)
- **Pattern matching** `instanceof` — 명시적 캐스트 불필요 (Java 16+)
- **Text blocks** — 여러 줄 문자열 (SQL, JSON 템플릿) (Java 15+)
- **Switch expressions** — 화살표 구문 (Java 14+)
- **Pattern matching in switch** — sealed 타입 완전 처리 (Java 21+)

```java
// 패턴 매칭 instanceof
if (shape instanceof Circle c) {
    return Math.PI * c.radius() * c.radius();
}

// Sealed 타입 계층
public sealed interface PaymentMethod permits CreditCard, BankTransfer, Wallet {}

// Switch 표현식
String label = switch (status) {
    case ACTIVE -> "Active";
    case SUSPENDED -> "Suspended";
    case CLOSED -> "Closed";
};
```

## Optional 사용법

- 결과가 없을 수 있는 finder 메서드에서 `Optional<T>` 반환
- `map()`, `flatMap()`, `orElseThrow()` 사용 — `isPresent()` 없이 `get()` 절대 금지
- `Optional`을 필드 타입이나 메서드 매개변수로 사용 금지

```java
// GOOD
return repository.findById(id)
    .map(ResponseDto::from)
    .orElseThrow(() -> new OrderNotFoundException(id));

// BAD — 매개변수로 Optional
public void process(Optional<String> name) {}
```

## 오류 처리

- 도메인 오류에는 unchecked 예외 선호
- `RuntimeException`을 확장하는 도메인별 예외 생성
- 최상위 핸들러 외에는 `catch (Exception e)` 지양
- 예외 메시지에 컨텍스트 포함

```java
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Order not found: id=" + id);
    }
}
```

## Streams

- 변환에 스트림 사용; 파이프라인을 짧게 유지 (최대 3-4 연산)
- 가독성이 있을 때 메서드 참조 선호: `.map(Order::getTotal)`
- 스트림 연산에서 부작용 방지
- 복잡한 로직은 복잡한 스트림 파이프라인보다 루프 선호

## 참고

skill: `java-coding-standards` — 예시가 포함된 전체 코딩 표준
skill: `jpa-patterns` — JPA/Hibernate 엔티티 설계 패턴
