---
description: 빌드 에러 자동 수정 — 빌드 도구를 감지하고 컴파일/테스트 오류를 수정합니다
argument-hint: [blank to auto-detect build errors]
---

# Build Fix

**Input**: $ARGUMENTS

---

## Step 1 — DETECT

```bash
[ -f "pom.xml" ] && echo "Maven" || \
[ -f "build.gradle.kts" ] && echo "Gradle KTS" || \
[ -f "build.gradle" ] && echo "Gradle" || \
echo "No build file found"
```

Java 버전 확인:
```bash
java -version 2>&1 | head -1
./mvnw --version 2>&1 | head -1 || ./gradlew --version 2>&1 | head -3
```

## Step 2 — BUILD

Maven:
```bash
./mvnw clean compile -q --no-transfer-progress 2>&1
```

Gradle:
```bash
./gradlew clean compileJava -q 2>&1
```

## Step 3 — ANALYZE ERRORS

오류 없으면: "빌드 성공. 수정할 오류가 없습니다."

오류 있으면 분류:
- **컴파일 오류** → `java-build-resolver` 에이전트
- **의존성 오류** → `java-build-resolver` 에이전트
- **테스트 실패** → 별도 분석 후 `java-build-resolver`

## Step 4 — FIX

`java-build-resolver` 에이전트에 위임:
- 오류 메시지 전달
- 빌드 도구 명시
- 영향 파일 목록 포함

**원칙**: 증상을 억제하지 않고 근본 원인을 수정. `@SuppressWarnings` 금지.

## Step 5 — VERIFY

수정 후:
```bash
# 컴파일 확인
./mvnw compile -q --no-transfer-progress 2>&1 | tail -3 || \
./gradlew compileJava -q 2>&1 | tail -3

# 테스트 확인
./mvnw test -q --no-transfer-progress 2>&1 | tail -5 || \
./gradlew test -q 2>&1 | tail -5
```

## Stop Condition

3회 시도 후에도 실패 시:
- 오류 상세 내용 보고
- 수동 개입 요청
- 아키텍처 변경이 필요한 경우 `planner` 에이전트 권장
