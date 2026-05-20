---
name: springboot-tdd
description: Test-driven development for Spring Boot using JUnit 5, Mockito, MockMvc, Testcontainers, and JaCoCo. Use when adding features, fixing bugs, or refactoring.
origin: ECC
---

# Spring Boot TDD Workflow

JUnit 5, Mockito, MockMvc, Testcontainers를 사용한 Spring Boot TDD — 80%+ 커버리지.

## When to Use

- 새 기능 또는 엔드포인트
- 버그 수정 또는 리팩토링
- 데이터 접근 로직 또는 보안 규칙 추가

## Workflow

1) 먼저 테스트 작성 (실패해야 함)
2) 통과하는 최소 코드 구현
3) 테스트 통과 상태에서 리팩토링
4) JaCoCo로 커버리지 강제 적용

## Unit Tests (JUnit 5 + Mockito)

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
  @Mock OrderRepository repo;
  @InjectMocks OrderService service;

  @Test
  @DisplayName("create — 유효한 요청으로 주문 생성")
  void create_validRequest_createsOrder() {
    var req = new CreateOrderRequest("Alice", BigDecimal.TEN);
    when(repo.save(any())).thenAnswer(inv -> {
      OrderEntity e = inv.getArgument(0);
      return new OrderEntity(1L, e.getCustomerName(), e.getAmount());
    });

    var result = service.create(req);

    assertThat(result.customerName()).isEqualTo("Alice");
    assertThat(result.id()).isEqualTo(1L);
    verify(repo).save(any());
  }

  @Test
  @DisplayName("findById — 존재하지 않는 ID로 예외 발생")
  void findById_notFound_throwsException() {
    when(repo.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findById(99L))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessageContaining("99");
  }
}
```

## Web Layer Tests (MockMvc)

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
  @Autowired MockMvc mockMvc;
  @MockBean OrderService orderService;

  @Test
  @DisplayName("GET /orders/{id} — 주문 반환")
  void getOrder_returns200() throws Exception {
    when(orderService.findById(1L)).thenReturn(sampleResponse());

    mockMvc.perform(get("/api/orders/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.customerName").value("Alice"));
  }

  @Test
  @DisplayName("POST /orders — 유효하지 않은 입력 시 400 반환")
  void createOrder_invalidInput_returns400() throws Exception {
    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"customerName":"","amount":-1}"""))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /orders/{id} — 존재하지 않는 ID 시 404 반환")
  void getOrder_notFound_returns404() throws Exception {
    when(orderService.findById(99L))
        .thenThrow(new OrderNotFoundException(99L));

    mockMvc.perform(get("/api/orders/99"))
        .andExpect(status().isNotFound());
  }
}
```

## Integration Tests (SpringBootTest)

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderIntegrationTest {
  @Autowired MockMvc mockMvc;

  @Test
  @DisplayName("주문 생성 → 조회 통합 테스트")
  void createAndRetrieveOrder() throws Exception {
    // 생성
    var result = mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"customerName":"Bob","amount":50}"""))
        .andExpect(status().isCreated())
        .andReturn();

    // ID 추출 후 조회
    var id = JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
    mockMvc.perform(get("/api/orders/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.customerName").value("Bob"));
  }
}
```

## Persistence Tests (DataJpaTest)

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainersConfig.class)
class OrderRepositoryTest {
  @Autowired OrderRepository repo;

  @Test
  @DisplayName("저장 및 조회")
  void saveAndFind() {
    var entity = new OrderEntity(null, "Alice", BigDecimal.TEN);
    var saved = repo.save(entity);

    var found = repo.findById(saved.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getCustomerName()).isEqualTo("Alice");
  }
}
```

## Testcontainers Config

```java
// src/test/java/com/example/config/TestContainersConfig.java
@Configuration
public class TestContainersConfig {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
      .withReuse(true);

  static {
    postgres.start();
  }

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }
}
```

## Coverage (JaCoCo)

Maven:
```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.14</version>
  <executions>
    <execution><goals><goal>prepare-agent</goal></goals></execution>
    <execution>
      <id>report</id>
      <phase>verify</phase>
      <goals><goal>report</goal></goals>
    </execution>
    <execution>
      <id>check</id>
      <goals><goal>check</goal></goals>
      <configuration>
        <rules>
          <rule>
            <limits>
              <limit>
                <counter>LINE</counter>
                <value>COVEREDRATIO</value>
                <minimum>0.80</minimum>
              </limit>
            </limits>
          </rule>
        </rules>
      </configuration>
    </execution>
  </executions>
</plugin>
```

## Assertions Cheatsheet

```java
// 기본
assertThat(result).isEqualTo(expected);
assertThat(list).hasSize(3).containsExactly(a, b, c);
assertThat(optional).isPresent().hasValue(expected);

// 예외
assertThatThrownBy(() -> service.method())
    .isInstanceOf(MyException.class)
    .hasMessageContaining("expected text");

// BigDecimal (정밀도 무관)
assertThat(price).isEqualByComparingTo(new BigDecimal("10.00"));

// JSON path
mockMvc.perform(get("/api/orders/1"))
    .andExpect(jsonPath("$.data.id").value(1))
    .andExpect(jsonPath("$.data.customerName").value("Alice"));
```

## CI Commands

- Maven: `./mvnw -T 4 test` 또는 `./mvnw verify`
- Gradle: `./gradlew test jacocoTestReport`

**Remember**: 테스트를 빠르게, 격리되게, 결정적으로 유지. 구현 세부사항이 아닌 동작을 테스트.
