# Authentication Module

- 사용자 인증/인가 담당  
- AWS Cognito + Google OIDC 기반의 로그인 지원
- Provider는 Google 외에 추가 예정

## 환경 설정
- Cognito, Google OIDC 설정은
  AWS Secrets Manager 또는 Parameter Store에서 관리
- port 번호: 5080

## 실행방법

### 운영 환경
```
  java -jar authentication.jar --spring.profiles.active=prod
```

### 개발 환경

- 개발 환경에서 Cognito 인증, Secrets Manager, Parameter Store 을 사용하려면
AWS CLI 자격 증명이 필요합니다.
- `aws configure` 명령으로 Access Key / Secret Key를 설정해주세요.

```
  java -jar authentication.jar --spring.profiles.active=dev
```

### 로컬 환경

- 로컬 환경은 AWS 리소스 의존성을 최소화하기 위해 Cognito 인증을 사용하지 않습니다.
- 로컬에서는 JWT 검증을 우회하거나 Mock Token을 사용해 개발할 수 있습니다.

```
  java -jar authentication.jar
```
