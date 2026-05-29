# CLAUDE.md

Java/Spring Boot 백엔드 API 프로젝트를 위한 Claude Code 가이드.

> **이 파일을 프로젝트에 맞게 커스터마이징하세요.** 특히 "프로젝트 개요"와 "아키텍처" 섹션.

## 프로젝트 개요

<!-- TODO: 프로젝트 목적과 주요 도메인을 여기에 작성하세요 -->
Spring Boot 기반 REST API 백엔드 서비스.

## 빌드 명령어

```bash
# Gradle
./gradlew compileJava             # 컴파일
./gradlew test                    # 단위 테스트
./gradlew check                   # 전체 테스트 + 린트
./gradlew bootRun                 # 로컬 실행
./gradlew jacocoTestReport        # 커버리지 리포트
```

## 아키텍처

3계층 표준 아키텍처:

```
Controller  →  Service  →  Repository  →  Database
   (HTTP)    (Business)    (Data Access)
```

```
src/main/java/com/example/
├── controller/      # REST 엔드포인트, 입력 검증
├── service/         # 비즈니스 로직, 트랜잭션
├── repository/      # Spring Data JPA 인터페이스
├── domain/          # 엔티티, VO, 도메인 모델
├── dto/             # 요청/응답 DTO (record 선호)
├── exception/       # 도메인 예외, 글로벌 핸들러
└── config/          # Spring 설정 클래스

src/test/java/com/example/
├── controller/      # @WebMvcTest
├── service/         # @ExtendWith(MockitoExtension.class)
├── repository/      # @DataJpaTest
└── integration/     # @SpringBootTest
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
5. **커밋 전**: `./mvnw verify` 또는 `./gradlew check` 통과 확인
6. **의존성 주입**: 필드 주입(`@Autowired`) 금지 — 생성자 주입 필수

## 스킬 참조

| 작업 | 스킬 |
|------|------|
| REST API 구조 설계 | `springboot-patterns` |
| JPA 엔티티/쿼리 최적화 | `jpa-patterns` |
| 보안 설정 | `springboot-security` |
| TDD 패턴 | `springboot-tdd` |
| 코딩 표준 | `java-coding-standards` |
| DB 마이그레이션 | `database-migrations` |
| PostgreSQL 쿼리/인덱스 | `postgres-patterns` |
| 헥사고날 아키텍처 | `hexagonal-architecture` |
| REST API 설계 원칙 | `api-design` |
| ADR 작성 | `architecture-decision-records` |
