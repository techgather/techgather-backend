---
name: java-coding-standards
description: "Java coding standards for Spring Boot services: naming, immutability, Optional usage, streams, exceptions, generics, and project layout."
origin: ECC
---

# Java Coding Standards

Spring Boot 서비스에서 읽기 쉽고 유지보수 가능한 Java (17+) 코드 표준.

## When to Activate

- Spring Boot 프로젝트에서 Java 코드 작성 또는 리뷰
- 명명, 불변성, 예외 처리 관례 강제
- records, sealed classes, pattern matching 작업 (Java 17+)
- Optional, streams, generics 사용 리뷰
- 패키지 및 프로젝트 레이아웃 구성

## Core Principles

- 영리함보다 명확성 우선
- 기본적으로 불변; 공유 가변 상태 최소화
- 의미 있는 예외로 빠른 실패
- 일관된 명명 및 패키지 구조

## Naming

```java
// PASS: 클래스/레코드: PascalCase
public class OrderService {}
public record Money(BigDecimal amount, Currency currency) {}

// PASS: 메서드/필드: camelCase
private final OrderRepository orderRepository;
public OrderResponse findByOrderNumber(String orderNumber) {}

// PASS: 상수: UPPER_SNAKE_CASE
private static final int MAX_PAGE_SIZE = 100;
private static final Duration TOKEN_EXPIRY = Duration.ofHours(24);

// PASS: 패키지: 소문자 역방향 도메인
// com.example.app.service
// com.example.app.controller
// com.example.app.domain
```

## Immutability

```java
// PASS: record와 final 필드 선호
public record OrderDto(Long id, String customerName, BigDecimal amount, OrderStatus status) {}

public class Order {
  private final Long id;
  private final String customerName;
  private final BigDecimal amount;

  // getter만, setter 없음
  public Long getId() { return id; }
}

// PASS: 컬렉션 방어적 복사
public List<OrderItem> getItems() {
  return List.copyOf(items); // 불변 복사 반환
}

// FAIL: 가변 컬렉션 직접 반환
public List<OrderItem> getItems() {
  return items; // 외부에서 수정 가능 — 위험
}
```

## Optional Usage

```java
// PASS: finder 메서드에서 Optional 반환
Optional<Order> findByOrderNumber(String orderNumber);

// PASS: map/flatMap 사용, get() 금지
return orderRepository.findByOrderNumber(orderNumber)
    .map(OrderResponse::from)
    .orElseThrow(() -> new OrderNotFoundException(orderNumber));

// PASS: 기본값
String name = Optional.ofNullable(dto.name()).orElse("Unknown");

// FAIL: Optional.get() 직접 사용
Order order = repo.findById(id).get(); // NoSuchElementException 위험

// FAIL: 매개변수로 Optional
public void process(Optional<String> name) {} // 그냥 null 허용으로
```

## Streams Best Practices

```java
// PASS: 변환에 스트림 사용, 파이프라인 짧게
List<String> activeNames = orders.stream()
    .filter(o -> o.status() == OrderStatus.ACTIVE)
    .map(Order::customerName)
    .sorted()
    .toList(); // Java 16+

// PASS: 메서드 참조
List<OrderResponse> responses = orders.stream()
    .map(OrderResponse::from) // 람다보다 간결
    .toList();

// FAIL: 복잡한 중첩 스트림 — 루프 사용
orders.stream()
    .flatMap(o -> o.items().stream()
        .filter(i -> i.price().compareTo(BigDecimal.TEN) > 0)
        .map(i -> new ItemDto(o.id(), i.name(), i.price())))
    .collect(Collectors.toList());
// → 이런 경우 명시적 루프가 더 명확
```

## Exceptions

```java
// PASS: 도메인 예외 — 컨텍스트 포함
public class OrderNotFoundException extends RuntimeException {
  public OrderNotFoundException(Long id) {
    super("Order not found: id=" + id);
  }
  public OrderNotFoundException(String orderNumber) {
    super("Order not found: orderNumber=" + orderNumber);
  }
}

// PASS: 기술적 예외 래핑
try {
  return externalPaymentApi.charge(request);
} catch (HttpClientErrorException ex) {
  throw new PaymentException("Payment failed: " + ex.getStatusCode(), ex);
}

// FAIL: 빈 catch 블록
try {
  doSomething();
} catch (Exception e) {} // 절대 금지

// FAIL: 너무 광범위한 예외 포착
try {
  service.process(request);
} catch (Exception e) { // RuntimeException 이상을 잡으면 안 됨
  log.error("Error", e);
}
```

## Modern Java Features

```java
// Records (Java 16+) — DTO에 이상적
public record CreateOrderRequest(
    @NotBlank String customerName,
    @NotNull @Positive BigDecimal amount) {}

// Sealed interfaces (Java 17+) — 닫힌 타입 계층
public sealed interface PaymentResult
    permits PaymentResult.Success, PaymentResult.Failure {
  record Success(String transactionId) implements PaymentResult {}
  record Failure(String errorCode, String message) implements PaymentResult {}
}

// Pattern matching instanceof (Java 16+)
if (event instanceof OrderCreatedEvent e) {
  log.info("Order created: id={}", e.orderId()); // 캐스트 불필요
}

// Switch expressions (Java 14+)
String statusLabel = switch (order.status()) {
  case PENDING -> "대기 중";
  case ACTIVE -> "처리 중";
  case COMPLETED -> "완료";
  case CANCELLED -> "취소됨";
};

// Text blocks (Java 15+) — SQL 쿼리에 유용
String query = """
    SELECT o.id, o.customer_name, o.amount
    FROM orders o
    WHERE o.status = :status
    ORDER BY o.created_at DESC
    """;
```

## Project Structure

```
src/main/java/com/example/
├── controller/          # REST 컨트롤러
│   └── OrderController.java
├── service/             # 비즈니스 로직
│   └── OrderService.java
├── repository/          # 데이터 접근
│   └── OrderRepository.java
├── domain/              # 도메인 모델/엔티티
│   ├── Order.java       # 도메인 객체
│   └── OrderEntity.java # JPA 엔티티
├── dto/                 # 요청/응답 DTO
│   ├── CreateOrderRequest.java
│   └── OrderResponse.java
├── exception/           # 도메인 예외
│   └── OrderNotFoundException.java
└── config/              # Spring 설정
    ├── SecurityConfig.java
    └── JpaConfig.java
```

## Logging Standards

```java
// PASS: SLF4J, 구조적 매개변수
private static final Logger log = LoggerFactory.getLogger(OrderService.class);

log.info("order_created id={} customer={}", order.getId(), order.getCustomerName());
log.warn("order_not_found id={}", id);
log.error("payment_failed orderId={} error={}", orderId, ex.getMessage(), ex);

// FAIL: 문자열 연결 (매개변수 사용)
log.info("Order created: " + order.getId()); // 성능 낭비

// FAIL: 민감 데이터 로깅
log.info("User login email={} password={}", email, password); // 절대 금지
```

## Testing Expectations

- `@ExtendWith(MockitoExtension.class)` 서비스 단위 테스트
- `@WebMvcTest` 컨트롤러 단위 테스트
- `@DataJpaTest` 레포지토리 테스트
- `@SpringBootTest` 통합 테스트에만
- JaCoCo 라인 커버리지 80% 최소

**Remember**: 명확하게 명명, 불변성 유지, 빠른 실패, 예외에 컨텍스트 포함, 모던 Java 기능으로 표현력 향상.
