---
name: jpa-patterns
description: JPA/Hibernate patterns for entity design, relationships, query optimization, transactions, auditing, indexing, pagination, and pooling in Spring Boot.
origin: ECC
---

# JPA/Hibernate Patterns

Spring Boot에서 데이터 모델링, 레포지토리, 성능 튜닝에 사용.

## When to Activate

- JPA 엔티티 및 테이블 매핑 설계
- 관계 정의 (@OneToMany, @ManyToOne, @ManyToMany)
- 쿼리 최적화 (N+1 방지, fetch 전략, 프로젝션)
- 트랜잭션, 감사, 소프트 삭제 설정
- 페이지네이션, 정렬, 커스텀 레포지토리 메서드 설정
- HikariCP 연결 풀링 또는 2레벨 캐싱 튜닝

## Entity Design

```java
@Entity
@Table(name = "orders", indexes = {
  @Index(name = "idx_orders_customer_id", columnList = "customer_id"),
  @Index(name = "idx_orders_status_created", columnList = "status, created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class OrderEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String customerName;

  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private OrderStatus status = OrderStatus.PENDING;

  @CreatedDate
  @Column(updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  private Instant updatedAt;

  @Version
  private Long version; // 낙관적 잠금
}
```

감사 활성화:
```java
@Configuration
@EnableJpaAuditing
class JpaConfig {}
```

## Relationships and N+1 Prevention

```java
// 부모 엔티티
@Entity
public class OrderEntity {
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<OrderItemEntity> items = new ArrayList<>();
}

// N+1 방지 — JOIN FETCH 사용
@Query("select o from OrderEntity o left join fetch o.items where o.id = :id")
Optional<OrderEntity> findWithItems(@Param("id") Long id);

// 또는 @EntityGraph 사용
@EntityGraph(attributePaths = {"items"})
Optional<OrderEntity> findById(Long id);
```

## Repository Patterns

```java
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
  // 파생 쿼리
  Page<OrderEntity> findByStatus(OrderStatus status, Pageable pageable);
  Optional<OrderEntity> findByOrderNumber(String orderNumber);

  // JPQL 쿼리
  @Query("select o from OrderEntity o where o.amount >= :minAmount and o.status = :status")
  Page<OrderEntity> findByMinAmountAndStatus(
      @Param("minAmount") BigDecimal minAmount,
      @Param("status") OrderStatus status,
      Pageable pageable);

  // 집계
  @Query("select count(o) from OrderEntity o where o.status = :status")
  long countByStatus(@Param("status") OrderStatus status);
}
```

## DTO Projections (성능 최적화)

엔티티 전체 로딩 없이 필요한 필드만 조회:

```java
// 인터페이스 프로젝션
public interface OrderSummary {
  Long getId();
  String getCustomerName();
  BigDecimal getAmount();
  OrderStatus getStatus();
}

Page<OrderSummary> findAllBy(Pageable pageable);

// 클래스 기반 프로젝션 (DTO 생성자)
public record OrderSummaryDto(Long id, String customerName, OrderStatus status) {}

@Query("select new com.example.dto.OrderSummaryDto(o.id, o.customerName, o.status) from OrderEntity o")
Page<OrderSummaryDto> findSummaries(Pageable pageable);
```

## Transactions

```java
@Service
public class OrderService {
  @Transactional
  public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
    OrderEntity entity = repo.findById(id)
        .orElseThrow(() -> new OrderNotFoundException(id));
    entity.setStatus(newStatus);
    return OrderResponse.from(entity); // dirty checking으로 자동 저장
  }

  @Transactional(readOnly = true)
  public Page<OrderResponse> findByStatus(OrderStatus status, Pageable pageable) {
    return repo.findByStatus(status, pageable).map(OrderResponse::from);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void auditOrderChange(Long orderId, String action) {
    // 별도 트랜잭션에서 감사 로그 저장
    auditRepo.save(new AuditLog(orderId, action));
  }
}
```

## Pagination

```java
// 기본 페이지네이션
PageRequest page = PageRequest.of(0, 20, Sort.by("createdAt").descending());
Page<OrderEntity> orders = repo.findByStatus(OrderStatus.PENDING, page);

// 멀티 정렬
PageRequest page = PageRequest.of(0, 20,
    Sort.by(Sort.Order.desc("status"), Sort.Order.asc("createdAt")));
```

## Indexing and Performance

- 공통 필터에 인덱스 추가 (`status`, `customerId`, FK)
- 쿼리 패턴에 맞는 복합 인덱스 (`status, created_at`)
- `select *` 지양 — 필요한 컬럼만 프로젝션
- `saveAll`과 `hibernate.jdbc.batch_size`로 배치 쓰기

## Connection Pooling (HikariCP)

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      validation-timeout: 5000
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true
```

## Migrations

- Flyway 또는 Liquibase 사용 — 프로덕션에서 Hibernate auto DDL 금지
- 마이그레이션은 멱등성과 추가적으로 유지; 계획 없이 컬럼 삭제 금지

```sql
-- V1__create_orders_table.sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_orders_status_created ON orders (status, created_at DESC);
```

## Testing Data Access

```bash
# SQL 디버깅
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE
```

**Remember**: 엔티티는 작게, 쿼리는 의도적으로, 트랜잭션은 짧게. fetch 전략과 프로젝션으로 N+1 방지, 읽기/쓰기 경로를 위한 인덱싱.
