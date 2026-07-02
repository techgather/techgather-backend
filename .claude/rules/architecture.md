---
paths:
  - "**/*.java"
  - "**/*.kt"
---
# Architecture

Spring Boot 3.5.6 / Java 17 + Kotlin 2.1.0 멀티모듈 프로젝트.

## 모듈별 패키지 구조

### api (Kotlin — REST API 서버)
```
api/src/main/kotlin/api/
├── controller/          # @RestController, @ResponseStatus
│   └── dto/
│       ├── request/     # 요청 DTO (data class 또는 일반 class)
│       └── response/    # 응답 DTO
├── service/             # @Service, @Transactional
│   └── dto/result/      # 서비스 결과 객체
├── annotation/          # 커스텀 어노테이션
├── aop/                 # @Aspect
├── util/                # 유틸리티
└── global/
    ├── config/          # SecurityConfig, SwaggerConfig, RdsDataSourceConfig 등
    ├── exception/       # GlobalExceptionHandler, ApiErrorResponse
    └── logging/         # 필터
```

### domain (Java — JPA 엔티티 + QueryDSL)
```
domain/src/main/java/domain/
├── entity/              # @Entity, @Getter, @NoArgsConstructor(PROTECTED)
├── repository/          # JpaRepository + Custom 인터페이스
│   └── impl/            # QueryDSL 구현체 (CustomXxxRepositoryImpl)
├── constants/           # Enum (PostStatus, Language, Role, AuthProvider)
├── common/              # BaseTime
├── config/              # QuerydslConfig
└── vo/                  # Value Object
```

### collector (Kotlin — 헥사고날 구조)
```
collector/src/main/kotlin/collector/
├── engine/              # 순수 도메인 로직 (CollectEngine)
│   ├── command/         # 커맨드 객체
│   ├── model/           # 도메인 모델
│   └── port/            # 포트 인터페이스 (Crawler, Extractor, Publisher 등)
├── adapter/             # 포트 구현체 (외부 기술 의존)
│   ├── crawler/         # jsoup HTML 크롤러
│   ├── fetcher/         # Ktor HTTP 클라이언트
│   ├── extractor/       # RSS/HTML 추출기
│   └── publisher/kafka/ # Kafka 발행
└── worker/              # 진입점 (CollectorRunner — Semaphore(8))
```

## 레이어 책임

| 레이어 | 책임 | 금지 사항 |
|--------|------|----------|
| Controller | HTTP 매핑, 입력 검증, 응답 포맷 | 비즈니스 로직 |
| Service | 비즈니스 로직, 트랜잭션 | 직접 HTTP 처리 |
| Repository | 데이터 접근, 쿼리 | 비즈니스 로직 |
| Entity | 도메인 상태, 정적 팩토리 | 외부 DTO 의존 |
| Port (collector) | 추상 인터페이스 | 구현 기술 의존 |
