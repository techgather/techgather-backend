---
description: 현재 Spring Boot 프로젝트를 분석하여 CLAUDE.md와 rules를 프로젝트에 맞게 자동 커스터마이징
argument-hint: (없음)
---

# Dev Init

현재 프로젝트의 빌드 파일, 패키지 구조, 코드 스타일을 분석하여 CLAUDE.md와 rules를 실제 프로젝트에 맞게 업데이트한다.

---

## Phase 1 — 빌드 파일 분석

다음 파일 중 존재하는 것을 읽는다.

- `build.gradle`
- `build.gradle.kts`
- `pom.xml`
- `settings.gradle`
- `settings.gradle.kts`

추출할 정보:
- **프로젝트명** (rootProject.name 또는 artifactId)
- **빌드 툴** (Gradle / Maven)
- **Java/Kotlin 버전**
- **Spring Boot 버전**
- **사용 중인 의존성** — 아래 목록 기준으로 감지

| 의존성 키워드 | 감지 결과 |
|---|---|
| `kafka` | Kafka 사용 |
| `redis`, `data-redis` | Redis 사용 |
| `querydsl` | QueryDSL 사용 |
| `security` | Spring Security 사용 |
| `webflux` | Reactive (WebFlux) 사용 |
| `mongodb` | MongoDB 사용 |
| `flyway` | Flyway 마이그레이션 |
| `liquibase` | Liquibase 마이그레이션 |
| `kotlin` | Kotlin 사용 |

---

## Phase 2 — 패키지 구조 분석

`src/main/java/` 하위를 스캔한다.

```bash
find src/main/java -type f -name "*.java" -o -name "*.kt" | head -50
```

추출할 정보:
- **베이스 패키지** (최상위 공통 경로, 예: `com.careminder.carenote.be`)
- **실제 레이어 디렉토리명** (controller vs web, entity vs domain/entity 등)
- **도메인 목록** (service 하위 서브디렉토리, 예: note, wallet, profile)
- **언어** (Java / Kotlin / 혼재)

---

## Phase 3 — 코드 스타일 샘플링

각 레이어에서 파일 1개씩 읽어 스타일을 파악한다.

| 레이어 | 읽을 파일 | 파악할 내용 |
|---|---|---|
| Controller | `*Controller.java` 중 1개 | 응답 래퍼 클래스명, URL 패턴, 어노테이션 |
| Entity | `*Entity.java` 또는 `domain/**/*.java` 중 1개 | 클래스 어노테이션, ID 타입, 감사 필드 |
| Service | `*Service.java` 중 1개 | 트랜잭션 방식, 생성자 주입 여부 |
| Repository | `*Repository.java` 중 1개 | 인터페이스 vs 구현체, QueryDSL 여부 |
| DTO | `*Dtos.java` 또는 `*Request.java` 중 1개 | record vs class, inner class 여부 |
| Test | `src/test/**/*.java` 중 1개 | 사용 어노테이션, 테스트 프레임워크 |

---

## Phase 4 — CLAUDE.md 업데이트

`CLAUDE.md`를 읽고 분석 결과로 아래 항목을 업데이트한다.

### 업데이트 규칙

1. **프로젝트 개요 섹션** — TODO 주석 제거, 실제 프로젝트명과 도메인 설명으로 교체
2. **빌드 명령어 섹션** — 감지된 빌드 툴(Gradle/Maven)에 맞는 명령어로 정리, 불필요한 것 제거
3. **아키텍처 섹션** — `com/example/` → 실제 베이스 패키지 경로로 교체, 실제 디렉토리 구조로 업데이트
4. **기술 스택 섹션** (없으면 추가) — 감지된 의존성 목록 삽입

### 예시 변환

Before:
```
src/main/java/com/example/
├── controller/
├── service/
├── repository/
├── domain/
├── dto/
```

After (실제 프로젝트 기준):
```
src/main/java/com/careminder/carenote/be/
├── web/          # REST 컨트롤러
├── service/      # 비즈니스 로직
│   ├── note/
│   ├── wallet/
│   └── profile/
├── repository/   # JPA + QueryDSL
├── domain/
│   ├── entity/
│   └── enums/
├── dto/
└── kafka/        # 감지된 경우
```

---

## Phase 5 — Rules 업데이트

분석 결과를 기반으로 `.claude/rules/` 파일들의 예시 코드를 업데이트한다.

| Rule 파일 | 업데이트 내용 |
|---|---|
| `architecture.md` | 베이스 패키지 경로 교체 |
| `controller-patterns.md` | 실제 응답 래퍼 클래스명 반영 |
| `entity-patterns.md` | 실제 Entity 어노테이션 스타일 반영 |
| `service-patterns.md` | 실제 트랜잭션 패턴 반영 |

---

## Phase 6 — 결과 리포트

```
Dev Init 완료
─────────────────────────────────────
프로젝트    <프로젝트명>
빌드 툴     Gradle / Maven
언어        Java 17 / Kotlin 1.9 (혼재)
패키지      com.example.myapp
도메인      note, wallet, profile (3개)

감지된 스택
  ✓ Spring Boot 3.5
  ✓ Spring Security
  ✓ QueryDSL 5.1
  ✓ Kafka
  ✗ Redis (미감지)
  ✗ WebFlux (미감지)

업데이트된 파일
  ✓ CLAUDE.md
  ✓ .claude/rules/architecture.md
  ✓ .claude/rules/controller-patterns.md
  ✓ .claude/rules/entity-patterns.md
  ✓ .claude/rules/service-patterns.md
─────────────────────────────────────
초기화 완료. 이제 /dev plan <기능> 으로 시작하세요.
```
