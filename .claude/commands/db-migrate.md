---
description: DB 마이그레이션 실행·상태 확인·롤백 — Flyway 또는 Liquibase 자동 감지
argument-hint: [info | migrate | rollback | repair | blank for status]
---

# DB Migrate

**Action**: $ARGUMENTS (기본값: `info`)

---

## Phase 1 — DETECT

빌드 도구와 마이그레이션 도구 감지:

```bash
# 빌드 도구
[ -f "pom.xml" ] && echo "Maven" || echo "Gradle"

# 마이그레이션 도구
grep -q "flyway" pom.xml build.gradle build.gradle.kts 2>/dev/null && echo "Flyway"
grep -q "liquibase" pom.xml build.gradle build.gradle.kts 2>/dev/null && echo "Liquibase"
```

마이그레이션 도구를 찾을 수 없으면 종료: "pom.xml 또는 build.gradle에서 Flyway/Liquibase 의존성을 찾을 수 없습니다."

마이그레이션 파일 위치 확인:
```bash
find src/main/resources -name "*.sql" -path "*/migration*" | head -10
find src/main/resources -name "*.sql" -path "*/db*" | head -10
```

## Phase 2 — INFO (현재 상태 확인)

`$ARGUMENTS`가 비어 있거나 `info`인 경우:

**Flyway (Maven)**:
```bash
./mvnw flyway:info -q 2>&1
```

**Flyway (Gradle)**:
```bash
./gradlew flywayInfo 2>&1
```

**Liquibase (Maven)**:
```bash
./mvnw liquibase:status -q 2>&1
```

**Liquibase (Gradle)**:
```bash
./gradlew liquibaseStatus 2>&1
```

현재 버전, 적용된 마이그레이션, 대기 중인 마이그레이션을 보고.

## Phase 3 — MIGRATE

`$ARGUMENTS`가 `migrate`인 경우:

### 사전 확인
```bash
# 대기 중인 마이그레이션 목록 확인
./mvnw flyway:info -q 2>&1 | grep "Pending"
```

대기 중인 마이그레이션이 있으면 목록을 보여주고 확인 요청.

### 실행
**Flyway (Maven)**:
```bash
./mvnw flyway:migrate 2>&1
```

**Flyway (Gradle)**:
```bash
./gradlew flywayMigrate 2>&1
```

**Liquibase (Maven)**:
```bash
./mvnw liquibase:update 2>&1
```

### 실행 후 검증
```bash
./mvnw flyway:info -q 2>&1 | tail -10
```

## Phase 4 — ROLLBACK

`$ARGUMENTS`가 `rollback`인 경우:

> **주의**: Flyway 무료 버전은 undo를 지원하지 않음. Liquibase는 rollbackCount 사용.

**Flyway Pro (Maven)**:
```bash
./mvnw flyway:undo 2>&1
```

**Liquibase (Maven)**:
```bash
./mvnw liquibase:rollback -Dliquibase.rollbackCount=1 2>&1
```

롤백 후 현재 상태 재확인.

## Phase 5 — REPAIR

`$ARGUMENTS`가 `repair`인 경우 (체크섬 불일치 또는 실패한 마이그레이션 수정):

**Flyway (Maven)**:
```bash
./mvnw flyway:repair 2>&1
```

## Output

```
DB Migration — <날짜>
Tool: Flyway | Liquibase
Build: Maven | Gradle
Action: info | migrate | rollback | repair

Current version: <version>
Applied: N
Pending: N

Status: SUCCESS | FAILED | NO_CHANGES
```

> 마이그레이션 파일 작성 시 `skill: database-migrations` 참조.
