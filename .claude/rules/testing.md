---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/build.gradle"
  - "**/build.gradle.kts"
---
# Java Testing

> This file extends [common/testing.md](../common/testing.md) with Java-specific content.

## 테스트 프레임워크

- **JUnit 5** (`@Test`, `@ParameterizedTest`, `@Nested`, `@DisplayName`)
- **AssertJ** — 유창한 단언문 (`assertThat(result).isEqualTo(expected)`)
- **Mockito** — 의존성 모킹
- **Testcontainers** — 데이터베이스나 서비스가 필요한 통합 테스트

## 테스트 구성

```
src/test/java/com/example/app/
  service/           # 서비스 레이어 단위 테스트
  controller/        # 웹 레이어 / API 테스트
  repository/        # 데이터 접근 테스트
  integration/       # 교차 레이어 통합 테스트
```

`src/main/java` 패키지 구조를 `src/test/java`에 미러링.

## 단위 테스트 패턴

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository);
    }

    @Test
    @DisplayName("findById가 존재하는 주문을 반환")
    void findById_existingOrder_returnsOrder() {
        var order = new Order(1L, "Alice", BigDecimal.TEN);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        var result = orderService.findById(1L);

        assertThat(result.customerName()).isEqualTo("Alice");
        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("findById가 주문 없을 때 예외 발생")
    void findById_missingOrder_throws() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(99L))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessageContaining("99");
    }
}
```

## 웹 레이어 테스트 (MockMvc)

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean OrderService orderService;

    @Test
    @DisplayName("POST /orders — 201 Created 반환")
    void createOrder_returns201() throws Exception {
        when(orderService.create(any())).thenReturn(orderResponse());

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"customerName":"Alice","amount":100}"""))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.customerName").value("Alice"));
    }
}
```

## 통합 테스트 (Testcontainers)

```java
@Testcontainers
class OrderRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    private OrderRepository repository;

    @BeforeEach
    void setUp() {
        var dataSource = new PGSimpleDataSource();
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUser(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());
        repository = new JdbcOrderRepository(dataSource);
    }

    @Test
    void save_and_findById() {
        var saved = repository.save(new Order(null, "Bob", BigDecimal.ONE));
        var found = repository.findById(saved.getId());
        assertThat(found).isPresent();
    }
}
```

## 테스트 슬라이스 선택

| 테스트 대상 | 사용할 어노테이션 |
|------------|----------------|
| 서비스 단위 테스트 | `@ExtendWith(MockitoExtension.class)` |
| 컨트롤러 테스트 | `@WebMvcTest` |
| JPA 레포지토리 테스트 | `@DataJpaTest` |
| 통합 테스트 | `@SpringBootTest` + `@AutoConfigureMockMvc` |

## 테스트 이름 규칙

- 메서드명: `methodName_scenario_expectedBehavior()`
- `@DisplayName`: 사람이 읽기 좋은 설명

## 커버리지 (JaCoCo)

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

## 참고

skill: `springboot-tdd` — MockMvc와 Testcontainers를 사용한 Spring Boot TDD 패턴
