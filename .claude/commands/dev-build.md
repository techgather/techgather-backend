---
description: Java 빌드 오류 진단 및 수정 — Maven/Gradle 컴파일/테스트 실패 자동 해결
argument-hint: [error message | blank to auto-detect]
---

# Java Build Fix

**Input**: $ARGUMENTS

---

## Phase 1 — DETECT BUILD TOOL

```bash
[ -f "pom.xml" ] && echo "Maven" || \
[ -f "build.gradle.kts" ] && echo "Gradle Kotlin DSL" || \
[ -f "build.gradle" ] && echo "Gradle" || \
echo "Unknown — 빌드 파일을 찾을 수 없습니다"
```

## Phase 2 — RUN BUILD

Maven:
```bash
./mvnw compile -q --no-transfer-progress 2>&1
```

Gradle:
```bash
./gradlew compileJava -q 2>&1
```

오류 없으면: "빌드 성공. 오류가 없습니다."

## Phase 3 — PARSE ERROR

오류 메시지 분석:
1. 오류 타입 식별 (컴파일, 의존성, 어노테이션 프로세서)
2. 영향 받는 파일과 라인 특정
3. 근본 원인 파악

## Phase 4 — DELEGATE TO java-build-resolver

`java-build-resolver` 에이전트에 위임:
- 오류 메시지 전달
- 영향 받는 파일 목록 포함
- 빌드 도구 (Maven/Gradle) 명시

## Phase 5 — VERIFY

수정 후 빌드 재실행:
```bash
./mvnw compile -q --no-transfer-progress 2>&1 || \
./gradlew compileJava -q 2>&1
```

성공 시:
```bash
./mvnw test -q --no-transfer-progress 2>&1 | tail -10 || \
./gradlew test -q 2>&1 | tail -10
```

## Output

```
Build Fix — <날짜>
Build Tool: Maven | Gradle
Initial Status: FAILED
Errors Fixed: N
Files Modified: <파일 목록>
Final Status: SUCCESS | FAILED
```
