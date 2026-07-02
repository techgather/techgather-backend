# CLAUDE.md

TechGather 백엔드 서비스를 위한 Claude Code 가이드.

## 프로젝트 개요

기술 아티클 수집·분류·제공 플랫폼의 백엔드. Kotlin/Java 멀티모듈 구조로 수집(collector), 분류(batch), API 제공(api), 인증(authentication), 도메인(domain), 공통(application) 6개 모듈로 구성.

**기술 스택**
- Spring Boot 3.5.6 / Java 17 / Kotlin 2.1.0
- MySQL (JPA + QueryDSL 5.0)
- Kafka (수집 메시지 발행/소비)
- Spring Batch (RSS 피드 분류 파이프라인)
- Spring Security + OAuth2 Resource Server (JWT, AWS Cognito)
- AWS Parameter Store, Secrets Manager
- Ktor 3.3 (collector HTTP 클라이언트)
- Lombok (Java 모듈), Snowflake ID

## 모듈 구조

| 모듈 | 언어 | 역할 |
|------|------|------|
| `api` | Kotlin | REST API 서버 (Spring Boot 실행) |
| `authentication` | Java | OAuth2/Cognito 인증 서버 (Spring Boot 실행) |
| `batch` | Java | Spring Batch + Kafka 소비 (RSS 분류) |
| `collector` | Kotlin | RSS/HTML 수집 + Kafka 발행 (헥사고날 구조) |
| `domain` | Java | JPA 엔티티 + QueryDSL 리포지토리 |
| `application` | Java | 공통 예외, Snowflake ID 생성기 |

## 빌드 명령어

```bash
# 전체 빌드
./gradlew build

# 모듈별 실행 (api / authentication / batch / collector)
./gradlew :api:bootRun
./gradlew :authentication:bootRun
./gradlew :batch:bootRun
./gradlew :collector:bootRun

# 테스트
./gradlew test
./gradlew :api:test

# QueryDSL Q-class 생성 (domain 모듈)
./gradlew :domain:compileJava

# JAR 빌드
./gradlew :api:bootJar
./gradlew :collector:bootJar
```

## 아키텍처

```
api/src/main/kotlin/api/
├── controller/          # REST 컨트롤러
│   └── dto/
│       ├── request/     # 요청 DTO
│       └── response/    # 응답 DTO
├── service/             # 비즈니스 로직 (@Transactional)
│   └── dto/result/      # 서비스 결과 DTO
├── annotation/          # 커스텀 어노테이션 (@Role)
├── aop/                 # AOP (RoleAspect)
├── util/                # JWT 파싱 등 유틸
└── global/
    ├── config/          # Spring 설정 (Security, Swagger, RDS, AWS)
    ├── exception/       # GlobalExceptionHandler, ApiErrorResponse
    └── logging/         # 요청 로깅 필터

domain/src/main/java/domain/
├── entity/              # JPA 엔티티 (Post, Category, Tag, User 등)
├── repository/          # JPA 인터페이스 + QueryDSL Custom 인터페이스
│   └── impl/            # QueryDSL 구현체 (CustomXxxRepositoryImpl)
├── constants/           # Enum (PostStatus, Language, Role, AuthProvider)
├── common/              # BaseTime (공통 감사 필드)
├── config/              # QueryDSL 설정
└── vo/                  # Value Object

collector/src/main/kotlin/collector/
├── engine/              # 도메인 (CollectEngine, 포트 인터페이스)
│   ├── command/         # 커맨드 객체
│   ├── model/           # 도메인 모델
│   └── port/            # 포트 인터페이스 (Crawler, Extractor, Publisher 등)
├── adapter/             # 포트 구현체
│   ├── crawler/         # jsoup HTML 크롤러
│   ├── fetcher/         # Ktor HTTP 클라이언트
│   ├── extractor/       # RSS/HTML 추출기
│   ├── deduplicator/    # 중복 제거
│   └── publisher/kafka/ # Kafka 발행
└── worker/              # CollectorRunner, CollectorRegistry (Semaphore(8) 동시성 제한)
```

## 개발 워크플로우

```
1. 기능 계획   → /dev plan  (코드베이스 분석 후 계획서 작성)
2. 구현 & 검증 → /dev run   (계획서 기반 구현 + 빌드 확인)
3. 테스트      → /dev test  (TDD 워크플로우, 테스트 먼저 작성)
4. 리뷰        → /dev review (로컬 변경 또는 PR 종합 리뷰)
5. 커밋        → /git commit
6. PR 생성     → /git pr
```

전체 파이프라인 한 번에: `/dev`

## 슬래시 커맨드

### dev — 개발 워크플로우

| 커맨드 | 설명 |
|--------|------|
| `/dev` | 계획 → 구현 → 테스트 전체 워크플로우 |
| `/dev init` | 프로젝트 분석 후 CLAUDE.md·rules 자동 커스터마이징 |
| `/dev plan` | 코드베이스 분석 후 구현 계획서 작성 |
| `/dev run` | 계획서 기반 코드 구현 및 검증 |
| `/dev test` | TDD 워크플로우 (테스트 먼저 작성) |
| `/dev review` | 로컬 변경(Java 특화) 또는 PR 종합 리뷰 |
| `/dev build` | 빌드 오류 진단 및 수정 |
| `/dev fix` | 빌드 에러 자동 수정 |
| `/dev verify` | 빌드·정적분석·테스트·커버리지·보안 전체 검증 |
| `/dev coverage` | 커버리지 분석 및 미달 영역 테스트 생성 |

### git — GitHub 워크플로우

| 커맨드 | 설명 |
|--------|------|
| `/git commit` | 변경사항 커밋 |
| `/git pr` | PR 자동 생성 (push → PR → CI 확인) |
| `/git issue` | 이슈 생성 (`bug` / `feat`) |

### 기타

| 커맨드 | 설명 |
|--------|------|
| `/db-migrate` | DB 마이그레이션 실행·상태 확인·롤백 |

## 에이전트

| 에이전트 | 용도 | 언제 사용 |
|---------|------|----------|
| `code-reviewer` | Java/Spring Boot 코드 리뷰 | 코드 수정 후 항상 |
| `java-build-resolver` | 빌드/컴파일 에러 수정 | 빌드 실패 시 |
| `security-reviewer` | 보안 취약점 분석 | 인증/인가/입력처리 변경 시 |
| `tdd-guide` | TDD 워크플로우 안내 | 새 기능/버그수정 시 |
| `planner` | 기능 구현 계획 수립 | 복잡한 기능 시작 전 |
| `database-reviewer` | DB 쿼리·스키마 최적화 | JPA/SQL 변경 시 |
| `java-performance-reviewer` | JVM·N+1·커넥션 풀·캐시 성능 분석 | 성능 이슈 발생 시 |

## 핵심 규칙

1. **코드 수정 후**: 반드시 `code-reviewer` 에이전트 실행
2. **새 기능**: TDD 워크플로우 준수 (테스트 RED → 구현 GREEN → 리팩토링)
3. **빌드 실패**: `java-build-resolver` 에이전트 사용, 증상 억제 금지
4. **보안 코드**: `security-reviewer` 실행 필수 (인증/DB쿼리/파일처리)
5. **커밋 전**: `./gradlew check` 통과 확인
6. **의존성 주입**: 필드 주입(`@Autowired`) 금지 — 생성자 주입 필수
7. **ID 생성**: DB auto-increment 금지 — `SnowFlake.getInstance().nextId()` 사용
8. **QueryDSL Q-class**: 새 엔티티 추가 후 `./gradlew :domain:compileJava` 실행 필수
9. **동시 수집**: `CollectorRunner`의 `Semaphore(8)` 유지 — 임의로 제거하거나 상향 금지

## 스킬 참조

| 작업 | 스킬 |
|------|------|
| REST API 구조 설계 | `springboot-patterns` |
| JPA 엔티티/쿼리 최적화 | `jpa-patterns` |
| 보안 설정 | `springboot-security` |
| TDD 패턴 | `springboot-tdd` |
| 코딩 표준 | `java-coding-standards` |
| DB 마이그레이션 | `database-migrations` |
| 헥사고날 아키텍처 (collector) | `hexagonal-architecture` |
| REST API 설계 원칙 | `api-design` |
| ADR 작성 | `architecture-decision-records` |
