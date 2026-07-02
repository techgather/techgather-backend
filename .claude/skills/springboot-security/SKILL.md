---
name: springboot-security
description: Spring Security best practices for authn/authz, validation, CSRF, secrets, headers, rate limiting, and dependency security in Java Spring Boot services.
origin: ECC
---

# Spring Boot Security Review

인증, 입력 처리, 엔드포인트 생성, 시크릿 처리 시 사용.

## When to Activate

- 인증 추가 (JWT, OAuth2, 세션 기반)
- 인가 구현 (@PreAuthorize, 역할 기반 접근)
- 사용자 입력 검증 (Bean Validation, 커스텀 검증기)
- CORS, CSRF, 보안 헤더 설정
- 시크릿 관리 (Vault, 환경 변수)
- 속도 제한 또는 무차별 대입 보호 추가
- 의존성 CVE 스캔

## Authentication

- 상태 없는 JWT 또는 취소 목록이 있는 불투명 토큰 선호
- 세션에는 `httpOnly`, `Secure`, `SameSite=Strict` 쿠키 사용
- `OncePerRequestFilter` 또는 리소스 서버로 토큰 검증

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      Authentication auth = jwtService.authenticate(token);
      SecurityContextHolder.getContext().setAuthentication(auth);
    }
    chain.doFilter(request, response);
  }
}
```

## Authorization

- 메서드 보안 활성화: `@EnableMethodSecurity`
- `@PreAuthorize("hasRole('ADMIN')")` 또는 `@PreAuthorize("@authz.canEdit(#id)")` 사용
- 기본 거부; 필요한 범위만 노출

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/users")
  public Page<UserDto> listUsers(Pageable pageable) {
    return userService.findAll(pageable);
  }

  @PreAuthorize("@authz.isOwner(#id, authentication)")
  @DeleteMapping("/users/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
```

## Input Validation

```java
// BAD: 검증 없음
@PostMapping("/users")
public User createUser(@RequestBody UserDto dto) {
  return userService.create(dto);
}

// GOOD: 검증된 DTO
public record CreateUserDto(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Email String email,
    @NotNull @Min(0) @Max(150) Integer age
) {}

@PostMapping("/users")
public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserDto dto) {
  return ResponseEntity.status(HttpStatus.CREATED)
      .body(userService.create(dto));
}
```

## SQL Injection Prevention

```java
// BAD: 문자열 연결
@Query(value = "SELECT * FROM users WHERE name = '" + name + "'", nativeQuery = true)

// GOOD: 파라미터화된 네이티브 쿼리
@Query(value = "SELECT * FROM users WHERE name = :name", nativeQuery = true)
List<User> findByName(@Param("name") String name);

// GOOD: Spring Data 파생 쿼리 (자동 파라미터화)
List<User> findByEmailAndActiveTrue(String email);
```

## Password Encoding

```java
@Bean
public PasswordEncoder passwordEncoder() {
  return new BCryptPasswordEncoder(12); // cost factor 12
}

public User register(CreateUserDto dto) {
  String hashedPassword = passwordEncoder.encode(dto.password());
  return userRepository.save(new User(dto.email(), hashedPassword));
}
```

## CSRF Protection

```java
// 순수 JWT API — stateless 인증
http
  .csrf(csrf -> csrf.disable())
  .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

// 세션 기반 브라우저 앱 — CSRF 활성화 유지
http.csrf(Customizer.withDefaults());
```

## Secrets Management

```yaml
# BAD: application.yml에 하드코딩
spring:
  datasource:
    password: mySecretPassword123

# GOOD: 환경 변수 플레이스홀더
spring:
  datasource:
    password: ${DB_PASSWORD}

# GOOD: Spring Cloud Vault
spring:
  cloud:
    vault:
      uri: https://vault.example.com
      token: ${VAULT_TOKEN}
```

## Security Headers

```java
http
  .headers(headers -> headers
    .contentSecurityPolicy(csp -> csp
      .policyDirectives("default-src 'self'; script-src 'self'"))
    .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
    .xssProtection(Customizer.withDefaults())
    .referrerPolicy(rp -> rp
        .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)));
```

## CORS Configuration

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
  CorsConfiguration config = new CorsConfiguration();
  config.setAllowedOrigins(List.of("https://app.example.com")); // 프로덕션에서 * 금지
  config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
  config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
  config.setAllowCredentials(true);
  config.setMaxAge(3600L);

  UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
  source.registerCorsConfiguration("/api/**", config);
  return source;
}

http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
```

## Rate Limiting (Bucket4j)

```java
@Component
public class RateLimitFilter extends OncePerRequestFilter {
  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  private Bucket createBucket() {
    return Bucket.builder()
        .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
        .build();
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    String clientIp = request.getRemoteAddr(); // ForwardedHeaderFilter 설정 시 정확한 IP 반환
    Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createBucket());

    if (bucket.tryConsume(1)) {
      chain.doFilter(request, response);
    } else {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
    }
  }
}
```

## Dependency Security

- CI에서 OWASP Dependency Check / Snyk 실행
- Spring Boot 및 Spring Security 지원 버전 유지
- 알려진 CVE 발견 시 빌드 실패

## Release Checklist

- [ ] 인증 토큰 검증 및 만료 확인
- [ ] 모든 민감한 경로에 인가 가드
- [ ] 모든 입력 검증 및 새니타이즈
- [ ] SQL 문자열 연결 없음
- [ ] 앱 타입에 맞는 CSRF 설정
- [ ] 시크릿 외부화, 커밋 없음
- [ ] 보안 헤더 설정
- [ ] API에 속도 제한
- [ ] 의존성 스캔 완료
- [ ] 로그에 민감 데이터 없음

**Remember**: 기본 거부, 입력 검증, 최소 권한, 설정으로 보안 우선.
