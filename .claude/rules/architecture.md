---
paths:
  - "**/*.java"
---
# Architecture

Spring Boot 3.x (Jakarta namespace) 표준 레이어드 아키텍처.

```
src/main/java/com/{company}/{app}/
├── web/           # REST controllers (@RestController)
├── service/       # 비즈니스 로직 (@Service, @Transactional)
├── repository/    # JPA + QueryDSL 데이터 접근
├── domain/
│   ├── entity/    # JPA 엔티티
│   ├── enums/     # 공유 열거형
│   └── value/     # Value objects
├── dto/           # 요청/응답 DTO (*Dtos.java 내 static inner record)
└── global/
    ├── config/    # Spring 설정
    ├── security/  # 인증/인가
    ├── error/     # 예외 및 핸들러
    └── util/      # 공통 유틸리티
```

## 레이어 책임

| 레이어 | 책임 | 금지 사항 |
|--------|------|----------|
| Controller | HTTP 매핑, 입력 검증, 응답 포맷 | 비즈니스 로직 |
| Service | 비즈니스 로직, 트랜잭션 | 직접 HTTP 처리 |
| Repository | 데이터 접근, 쿼리 | 비즈니스 로직 |
| Entity | 도메인 상태 | 외부 DTO 의존 |
