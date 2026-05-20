---
description: Spring Boot 전체 검증 파이프라인 — 빌드·정적 분석·테스트·커버리지·보안 스캔 순차 실행
argument-hint: [--fast | --full | blank for standard]
---

# Verify

Spring Boot 프로젝트의 전체 품질 게이트를 순차적으로 실행합니다.

**Mode**: $ARGUMENTS
- 빈 값 / `--standard`: 빌드 + 테스트 + 커버리지
- `--fast`: 빌드 + 테스트만 (커버리지·보안 스킵)
- `--full`: 빌드 + 테스트 + 커버리지 + 보안 스캔 + 정적 분석

---

## Phase 1 — BUILD

```bash
./mvnw compile -q --no-transfer-progress 2>&1 | tail -5 || \
./gradlew compileJava -q 2>&1 | tail -5
```

실패 시 즉시 중단 → `/dev build` 실행 권장.

## Phase 2 — STATIC ANALYSIS

`--fast` 모드는 건너뜀.

```bash
# Checkstyle
./mvnw checkstyle:check -q 2>&1 | tail -5 || \
./gradlew checkstyleMain -q 2>&1 | tail -5

# SpotBugs
./mvnw spotbugs:check -q 2>&1 | tail -5 || \
./gradlew spotbugsMain -q 2>&1 | tail -5
```

오류 있으면 파일·라인·규칙 보고. 계속 진행 (차단하지 않음).

## Phase 3 — TEST + COVERAGE

```bash
./mvnw test -q --no-transfer-progress 2>&1 | tail -10 || \
./gradlew test -q 2>&1 | tail -10
```

테스트 실패 시 실패한 테스트 목록 보고 후 중단.

커버리지 리포트 생성:
```bash
./mvnw jacoco:report -q 2>&1 || \
./gradlew jacocoTestReport -q 2>&1

# 커버리지 수치 확인
grep -A3 "LINE" target/site/jacoco/index.html 2>/dev/null | \
  grep -oP '\d+%' | head -1 || echo "커버리지 리포트를 찾을 수 없습니다"
```

80% 미만이면 경고.

## Phase 4 — SECURITY SCAN

`--full` 모드에서만 실행.

```bash
# OWASP Dependency Check
./mvnw dependency-check:check -q 2>&1 | grep -E "CRITICAL|HIGH|One or more" | head -10 || \
./gradlew dependencyCheckAnalyze -q 2>&1 | grep -E "CRITICAL|HIGH" | head -10
```

CRITICAL 취약점 발견 시 → `security-reviewer` 에이전트 실행 권장.

## Phase 5 — SUMMARY REPORT

```
Verify — <날짜>  Mode: standard | fast | full
────────────────────────────────────────────
Phase 1  Build              PASS | FAIL
Phase 2  Static Analysis    PASS | FAIL | SKIP   (N issues)
Phase 3  Tests              PASS | FAIL           (N/N passed)
         Coverage           XX%  (≥80% required)
Phase 4  Security Scan      PASS | FAIL | SKIP   (N CVEs)
────────────────────────────────────────────
Overall: PASS | FAIL

Next steps:
  - FAIL Build    → /dev build
  - FAIL Tests    → /java-test
  - Low Coverage  → /test-coverage
  - CVEs found    → security-reviewer 에이전트
```

> 상세 검증 패턴은 `skill: springboot-verification` 참조.
