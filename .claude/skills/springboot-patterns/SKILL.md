---
name: springboot-patterns
description: Spring Boot architecture patterns, REST API design, layered services, data access, caching, async processing, and logging. Use for Java Spring Boot backend work.
origin: ECC
---

# Spring Boot Development Patterns

Spring Boot 아키텍처 및 API 패턴 — 확장 가능한 프로덕션 등급 서비스.

## When to Activate

- Spring MVC 또는 WebFlux로 REST API 구축
- controller → service → repository 레이어 구조화
- Spring Data JPA, 캐싱, 비동기 처리 설정
- 검증, 예외 처리, 페이지네이션 추가
- dev/staging/production 프로파일 설정
- Spring Events 또는 Kafka를 사용한 이벤트 기반 패턴 구현

## REST API Structure

```java
@RestController
@RequestMapping("/api/orders")
@Validated
class OrderController {
  private final OrderService orderService;

  OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping
  ResponseEntity<Page<OrderResponse>> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(
        orderService.list(PageRequest.of(page, size)));
  }

  @PostMapping
  ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(orderService.create(request));
  }

  @GetMapping("/{id}")
  ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(orderService.findById(id));
  }

  @DeleteMapping("/{id}")
  ResponseEntity<Void> delete(@PathVariable Long id) {
    orderService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
```

## Repository Pattern (Spring Data JPA)

```java
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
  @Query("select o from OrderEntity o where o.status = :status order by o.createdAt desc")
  Page<OrderEntity> findByStatus(@Param("status") OrderStatus status, Pageable pageable);

  Optional<OrderEntity> findByOrderNumber(String orderNumber);
}
```

## Service Layer with Transactions

```java
@Service
public class OrderService {
  private final OrderRepository repo;

  public OrderService(OrderRepository repo) {
    this.repo = repo;
  }

  @Transactional
  public OrderResponse create(CreateOrderRequest request) {
    OrderEntity entity = OrderEntity.from(request);
    OrderEntity saved = repo.save(entity);
    return OrderResponse.from(saved);
  }

  @Transactional(readOnly = true)
  public OrderResponse findById(Long id) {
    return repo.findById(id)
        .map(OrderResponse::from)
        .orElseThrow(() -> new OrderNotFoundException(id));
  }

  @Transactional(readOnly = true)
  public Page<OrderResponse> list(Pageable pageable) {
    return repo.findAll(pageable).map(OrderResponse::from);
  }

  @Transactional
  public void delete(Long id) {
    if (!repo.existsById(id)) {
      throw new OrderNotFoundException(id);
    }
    repo.deleteById(id);
  }
}
```

## DTOs and Validation

```java
public record CreateOrderRequest(
    @NotBlank @Size(max = 100) String customerName,
    @NotNull @Positive BigDecimal amount,
    @NotNull @FutureOrPresent LocalDate deliveryDate) {}

public record OrderResponse(Long id, String customerName, BigDecimal amount, OrderStatus status) {
  static OrderResponse from(OrderEntity entity) {
    return new OrderResponse(
        entity.getId(), entity.getCustomerName(),
        entity.getAmount(), entity.getStatus());
  }
}
```

## Exception Handling

```java
@RestControllerAdvice
class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(EntityNotFoundException.class)
  ResponseEntity<ApiResponse<Void>> handleNotFound(EntityNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> e.getField() + ": " + e.getDefaultMessage())
        .collect(Collectors.joining(", "));
    return ResponseEntity.badRequest().body(ApiResponse.error(message));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
    log.error("Unexpected error", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error("Internal server error"));
  }
}
```

## Caching

`@EnableCaching` 설정 클래스에 추가 필요.

```java
@Service
public class OrderCacheService {
  private final OrderRepository repo;

  public OrderCacheService(OrderRepository repo) { this.repo = repo; }

  @Cacheable(value = "order", key = "#id")
  public OrderResponse getById(Long id) {
    return repo.findById(id)
        .map(OrderResponse::from)
        .orElseThrow(() -> new OrderNotFoundException(id));
  }

  @CacheEvict(value = "order", key = "#id")
  public void evict(Long id) {}
}
```

## Async Processing

`@EnableAsync` 설정 클래스에 추가 필요.

```java
@Service
public class NotificationService {
  @Async
  public CompletableFuture<Void> sendAsync(Notification notification) {
    // 이메일/SMS 발송
    return CompletableFuture.completedFuture(null);
  }
}
```

## Logging (SLF4J)

```java
@Service
public class OrderService {
  private static final Logger log = LoggerFactory.getLogger(OrderService.class);

  public OrderResponse create(CreateOrderRequest request) {
    log.info("create_order customerName={}", request.customerName());
    try {
      // 로직
      log.info("order_created id={}", saved.getId());
      return OrderResponse.from(saved);
    } catch (Exception ex) {
      log.error("create_order_failed customerName={}", request.customerName(), ex);
      throw ex;
    }
  }
}
```

## Request Logging Filter

```java
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    long start = System.currentTimeMillis();
    try {
      filterChain.doFilter(request, response);
    } finally {
      long duration = System.currentTimeMillis() - start;
      log.info("req method={} uri={} status={} durationMs={}",
          request.getMethod(), request.getRequestURI(),
          response.getStatus(), duration);
    }
  }
}
```

## Pagination and Sorting

```java
PageRequest page = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());
Page<OrderEntity> results = orderRepository.findAll(page);
```

## Production Defaults

- 생성자 주입 선호, 필드 주입 지양
- Spring Boot 3+: `spring.mvc.problemdetails.enabled=true` (RFC 7807)
- HikariCP 풀 크기 설정, 타임아웃 구성
- 조회에 `@Transactional(readOnly = true)` 사용
- `@NonNull`과 `Optional`로 null 안전성 강제

**Remember**: 컨트롤러는 얇게, 서비스는 집중되게, 레포지토리는 단순하게, 오류는 중앙에서 처리.
