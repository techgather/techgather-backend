---
description: Java TDD 워크플로우 — 테스트 먼저 작성하고 Spring Boot 기능을 구현합니다
argument-hint: <feature, class, or method to test>
---

# Java TDD

**Feature**: $ARGUMENTS

---

## 개요

Spring Boot TDD 워크플로우: RED → GREEN → REFACTOR → COVERAGE

---

## Phase 1 — PLAN

`$ARGUMENTS`를 분석:
1. 어떤 계층을 테스트하는가? (서비스/컨트롤러/레포지토리)
2. 정상 경로 (happy path)는?
3. 실패 케이스 (에러/엣지케이스)는?
4. 필요한 모의 객체 (Mock)는?

**복잡한 기능**이면 먼저 `planner` 에이전트로 구현 계획 수립.

## Phase 2 — RED (테스트 작성)

`tdd-guide` 에이전트로 테스트 작성:

### 테스트 슬라이스 선택
- 서비스 로직 → `@ExtendWith(MockitoExtension.class)`
- 컨트롤러/API → `@WebMvcTest`
- JPA 레포지토리 → `@DataJpaTest`
- 통합 → `@SpringBootTest`

### 테스트 실행 (실패 확인)
```bash
./mvnw test -Dtest=<TestClassName> -q 2>&1 | tail -20 || \
./gradlew test --tests "<full.package.TestClassName>" 2>&1 | tail -20
```

실패 확인 — 테스트가 통과하면 이미 구현이 있거나 테스트가 잘못됨.

## Phase 3 — GREEN (최소 구현)

테스트를 통과하는 최소한의 코드 작성:
- 과도한 구현 금지
- 리팩토링 금지 (이 단계에서)

```bash
# 테스트 재실행
./mvnw test -Dtest=<TestClassName> -q 2>&1 | tail -10
```

모든 테스트 통과 확인.

## Phase 4 — REFACTOR

테스트 통과 상태에서:
1. 중복 제거
2. 명명 개선
3. 메서드 분리
4. 매 변경 후 테스트 재실행

```bash
./mvnw test -q 2>&1 | tail -5
```

## Phase 5 — COVERAGE

```bash
./mvnw test jacoco:report -q 2>&1 | tail -5
./gradlew test jacocoTestReport 2>&1 | tail -5
```

80% 미만이면 누락된 경로 테스트 추가.

## Output

```
TDD Cycle — <기능명>
Tests Written: N
RED: FAIL (확인됨)
GREEN: PASS
Coverage: XX%
Status: COMPLETE | NEEDS_MORE_TESTS
```
