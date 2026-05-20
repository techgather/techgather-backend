---
description: 코드 리뷰 — 로컬 변경사항(Java 특화) 또는 GitHub PR 리뷰 (PR 번호/URL 전달 시 PR 모드)
argument-hint: [pr-number | pr-url | file-path | blank for all changes]
---

# Dev Review

**Input**: $ARGUMENTS

---

## Mode Selection

`$ARGUMENTS`에 PR 번호, PR URL, 또는 `--pr`이 포함된 경우:
→ **PR Review Mode**로 이동

그 외:
→ **Local Review Mode** 사용

---

## Local Review Mode

### Phase 1 — GATHER

```bash
git diff --name-only HEAD
```

특정 파일 지정 시 (`$ARGUMENTS`):
```bash
git diff HEAD -- "$ARGUMENTS"
```

변경된 파일 없으면 종료: "리뷰할 내용이 없습니다."

### Phase 2 — BUILD CHECK

```bash
./mvnw compile -q --no-transfer-progress 2>&1 | tail -5 || \
./gradlew compileJava -q 2>&1 | tail -5
```

컴파일 실패 시 → `/dev build` 실행 먼저.

### Phase 3 — REVIEW

Java 파일이 포함된 경우 `java-reviewer` 에이전트에 위임한다.
그 외 파일은 직접 리뷰한다.

**Security Issues (CRITICAL)**
- 하드코딩된 자격 증명, API 키, 토큰
- SQL 인젝션 취약점 (문자열 연결)
- 입력 검증 누락 (`@Valid` 없는 `@RequestBody`)
- 경로 순회 위험

**Spring Boot Architecture (HIGH)**
- 필드 주입 (`@Autowired`) — 생성자 주입으로 전환
- 컨트롤러에 비즈니스 로직
- `@Transactional` 잘못된 레이어
- 엔티티 직접 반환 (DTO 없음)

**JPA Issues (HIGH)**
- `FetchType.EAGER` on collections
- 페이지네이션 없는 목록 엔드포인트
- `Optional.get()` without `isPresent()`

**Code Quality (MEDIUM)**
- 50줄 이상 함수
- 800줄 이상 파일
- 중첩 깊이 > 4
- 오류 처리 누락, 빈 catch 블록

### Phase 4 — REPORT

```
Dev Review — <날짜>
Files Changed: N

CRITICAL: N issues
  - [파일:라인] 문제 → 수정 방법

HIGH: N issues
  - [파일:라인] 문제 → 수정 방법

MEDIUM: N issues
  - [파일:라인] 문제

Decision: APPROVE | REQUEST_CHANGES | BLOCK
```

CRITICAL/HIGH → 커밋 차단.

---

## PR Review Mode

GitHub PR 종합 리뷰.

### Phase 1 — FETCH

```bash
gh pr view <NUMBER> --json number,title,body,author,baseRefName,headRefName,changedFiles,additions,deletions
gh pr diff <NUMBER>
```

### Phase 2 — REVIEW

변경된 파일 **전체** 읽기 (diff만 아님).

| 카테고리 | 확인 내용 |
|---------|---------|
| **Correctness** | 로직 오류, null 처리, 엣지 케이스 |
| **Security** | 인젝션, 인가 누락, 시크릿 노출 |
| **Spring Boot** | 계층 아키텍처, @Transactional, DTO |
| **JPA** | N+1, 페이지네이션, fetch 전략 |
| **Tests** | 커버리지, 테스트 슬라이스, 명명 |
| **Performance** | N+1 쿼리, 무제한 조회, 인덱스 |

### Phase 3 — VALIDATE

```bash
./mvnw verify -q --no-transfer-progress 2>&1 | tail -10 || \
./gradlew check -q 2>&1 | tail -10
```

### Phase 4 — DECIDE

| 조건 | 결정 |
|------|------|
| CRITICAL/HIGH 없음, 검증 통과 | **APPROVE** |
| MEDIUM만 있음 | **APPROVE** with comments |
| HIGH 있음 | **REQUEST CHANGES** |
| CRITICAL 있음 | **BLOCK** |

### Phase 5 — PUBLISH

```bash
# APPROVE
gh pr review <NUMBER> --approve --body "<요약>"

# REQUEST CHANGES
gh pr review <NUMBER> --request-changes --body "<필수 수정 요약>"
```
