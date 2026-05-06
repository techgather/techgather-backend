# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build individual modules
./gradlew :api:bootJar
./gradlew :collector:bootJar
./gradlew :batch:clean build
./gradlew clean build          # All modules

# Run tests
./gradlew test                 # All tests
./gradlew :api:test            # Single module
./gradlew :api:test --tests "api.PostServiceTest"  # Single test class

# Local development (Docker Compose spins up MySQL + Kafka)
docker-compose up -d
```

## Module Architecture

This is a **Spring Boot 3.5.6 multi-module Gradle project** (Kotlin + Java, JDK 17/21) for tech blog aggregation.

```
settings.gradle modules:
  api          – REST API server (Kotlin, port 8888)
  authentication – OAuth2/OIDC server via AWS Cognito (Java, port 5080)
  batch        – Kafka consumer + bulk DB writer via Spring Batch (Java, port 7500)
  collector    – Web scraper that publishes to Kafka (Kotlin)
  domain       – JPA entities, repositories, QueryDSL queries (Java, shared library)
  application  – Shared exceptions, SnowFlake ID generator, Role enum (Java, shared library)
```

### Data Flow

```
collector (RSS/HTML scrape)
  → Kafka topic "post"
    → batch (Spring Batch job, chunk size 100)
      → MySQL (domain layer)
        → api (REST, JWT-secured)
          → frontend
```

#### Key Architectural Patterns

- **Domain module** is a pure library (no Spring Boot main class). All JPA entities and repositories live here and are shared by `api` and `batch`.
- **Application module** provides shared exceptions (`TechGatherException` hierarchy with `*ErrorCode` enums) and the `SnowFlake` distributed ID generator used for `Post` IDs.
- **API module** uses AOP for authorization: `@Role` annotation + `RoleAspect`. JWT claims are extracted by `CustomJwtAuthenticationConverter`. Admin endpoints are under `/api/admin/**`.
- **Collector** follows a hexagonal adapter pattern: `HtmlCrawler` → `RssV2Extractor`/`AtomExtractor` → `ThumbnailDownloader` → `KafkaPublisher`. Sources are provider-configured (24 Korean tech company blogs). Scheduled daily at 3 AM Seoul time (`"0 0 3 * * *"`).
- **QueryDSL** is used for complex queries alongside Spring Data JPA. Custom repositories are named `Custom*Repository` with implementations in `*RepositoryImpl`. Q-classes are generated at compile time into `build/generated/` — never commit them.
- **Dual write strategy**: API and domain use JPA (Hibernate, batch_size=100). Batch module uses JDBC native SQL via `NamedParameterJdbcTemplate` for bulk upserts (`ON DUPLICATE KEY UPDATE`).

## Key Entities & Data Model

- `Post` — main content entity; `postId` is Snowflake-generated (Long)
- `User` — OAuth user; `userId` is a String from the OAuth provider
- `Category` / `CategoryGroup` / `PostCategory` — taxonomy (many-to-many via `PostCategory`)
- `Tag` / `PostTag` — tagging (normalized via `TagNormalizerUtils`)
- `BaseTime` — base class providing `createdAt` / `updatedAt` for all entities

## Error Handling Convention

Custom exceptions follow a strict hierarchy:
```
TechGatherException (base)
  ↳ takes a TechGatherErrorCode
      ↳ CommonClientErrorCode, CommonServerErrorCode, PostErrorCode
```
`GlobalExceptionHandler` in the API module maps these to `ApiErrorResponse`. Always use the existing error code enums rather than throwing raw exceptions.

## Kafka

- **Topic**: `post` — collector publishes, batch consumes
- **Consumer group**: `batch-consumer-group`
- **Serialization**: key=`StringSerializer`, value=`JsonSerializer` (Jackson). Consumer trusts all packages (`spring.json.trusted.packages: "*"`)
- **DTO**: `RssFeedMessage` is the Kafka payload exchanged between collector and batch

## Testing

- **Framework**: JUnit 5 (Jupiter) for all modules; Kotlin modules use `kotlin-test-junit5`
- **Available test slices**: `spring-boot-starter-test`, `spring-security-test`, `spring-batch-test`, `spring-kafka-test`
- No mocking framework (MockK/Mockito) is currently configured

## Configuration & Secrets

- Profile `dev` is the default (local); `docker` is for containerized; `prod` for production.
- Sensitive values come from **AWS Secrets Manager** (`dev/db/parameter`) and **AWS Parameter Store** (prefix: `/tech-gather/dev/`) — not `.env` files.
- AWS region: `ap-northeast-2` (Seoul). Local dev requires valid `aws configure` credentials.
- Local dev connects to a **remote RDS** instance — Docker Compose only spins up Kafka, not MySQL.

## CI/CD

GitHub Actions workflows in `.github/workflows/`:
- `api.yml` triggers on push to `development` or `cicd` branches when files under `api/**`, `application/**`, `domain/**`, or Gradle files change. It builds `:api:bootJar`, pushes a Docker image to AWS ECR, and deploys via SSH.
- Collector CI/CD is currently disabled (commented out in workflow).

## Language Split

- **Kotlin**: `api` and `collector` modules
- **Java**: `authentication`, `batch`, `domain`, `application` modules

Avoid mixing languages within a module. New code should match the existing language for that module.
