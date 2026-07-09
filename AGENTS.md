# Repository Guidelines

## Project Structure & Module Organization

This is a Spring Boot multi-module Gradle repository for tech blog aggregation.

- `api/`: Kotlin REST API server and controllers, services, DTOs, security config.
- `collector/`: Kotlin crawler/scraper that publishes collected posts to Kafka.
- `batch/`: Java Spring Batch/Kafka consumer that writes posts and tags to MySQL.
- `domain/`: Java JPA entities, repositories, QueryDSL custom queries, shared data model.
- `application/`: Java shared exceptions, error codes, roles, and SnowFlake ID generation.
- `authentication/`: Java OAuth2/OIDC authentication service using AWS Cognito.

Tests live under each module’s `src/test`. QueryDSL Q-classes under `build/generated/` must not be committed.

## Build, Test, and Development Commands

```bash
./gradlew clean build          # Build and test all modules
./gradlew :api:bootJar         # Package the API service
./gradlew :collector:bootJar   # Package the collector service
./gradlew :batch:clean build   # Build the batch module
./gradlew test                 # Run all tests
./gradlew :api:test            # Run tests for one module
docker-compose up -d           # Start local infrastructure such as Kafka
```

Profiles include `dev`, `docker`, and `prod`. Local development may require AWS credentials because secrets are loaded from AWS.

## Coding Style & Naming Conventions

Use each module’s existing language: Kotlin in `api` and `collector`; Java in `domain`, `application`, `batch`, and `authentication`. Name Spring components by role, such as `PostService`, `PostController`, `CustomPostRepository`, and `CustomPostRepositoryImpl`.

Prefer existing patterns over new abstractions. Use QueryDSL for complex dynamic queries and Spring Data JPA for simple repository operations.

## Testing Guidelines

The project uses JUnit 5. Kotlin modules use `kotlin-test-junit5`; Spring test dependencies include security, batch, and Kafka support. Add focused tests for service logic, repository behavior, and authorization-sensitive API changes.

Run the narrowest relevant test first, then broader tests before submitting:

```bash
./gradlew :api:test --tests "api.PostServiceTest"
./gradlew test
```

## Commit & Pull Request Guidelines

Recent commits use short conventional prefixes, usually `fix:`, followed by a module or concise Korean description, for example `fix: Collector | kakao-pay 블로그 추가`. Keep commits small and scoped.

Pull requests should include a summary, affected modules, test results, and linked issues when applicable. Include screenshots or API examples for visible response or Swagger changes.

## Security & Configuration Tips

Do not commit secrets, local credentials, or build output. Configuration values come from AWS Secrets Manager and Parameter Store. Document any new required parameter names in the PR.
