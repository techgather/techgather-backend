---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/build.gradle"
  - "**/build.gradle.kts"
---
# Java Hooks

> This file extends [common/hooks.md](../common/hooks.md) with Java-specific content.

## PostToolUse 훅

`.claude/settings.json`에 설정:

### 컴파일 검증

Java 파일 편집 후 자동 컴파일:

```json
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Edit|Write",
        "command": "if echo \"$FILE_PATH\" | grep -q '\\.java$'; then (./mvnw compile -q --no-transfer-progress 2>&1 | tail -5 || ./gradlew compileJava -q 2>&1 | tail -5) && echo '[Hook] Compile OK' || echo '[Hook] Compile failed'; fi",
        "description": "Java 파일 편집 후 컴파일 검증"
      }
    ]
  }
}
```

### Checkstyle 검사

```json
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Edit|Write",
        "command": "if echo \"$FILE_PATH\" | grep -q '\\.java$'; then ./mvnw checkstyle:check -q 2>&1 | tail -3 || echo '[Hook] Checkstyle failed'; fi",
        "description": "Java 파일 편집 후 스타일 검사"
      }
    ]
  }
}
```

## Stop 훅

### 최종 빌드 검증

세션 종료 시:

```json
{
  "hooks": {
    "Stop": [
      {
        "command": "(./mvnw verify -q --no-transfer-progress 2>&1 | tail -10 || ./gradlew check -q 2>&1 | tail -10) && echo '[Hook] Final verify OK' || echo '[Hook] Verify failed'",
        "description": "세션 종료 시 전체 빌드/테스트 검증"
      }
    ]
  }
}
```

## 권장 순서

1. 컴파일 검증 (빠른 피드백)
2. Checkstyle (스타일 강제)
3. 최종 `verify` (커버리지 포함 전체 검사)
