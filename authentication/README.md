# Authentication Module

- 사용자 인증/인가 담당  
- AWS Cognito + Google OIDC 기반의 로그인 지원
- Provider는 Google 외에 추가 예정

## 환경 설정
- Cognito, Google OIDC 설정은
  AWS Secrets Manager 또는 Parameter Store에서 관리
- application.yml
~~~
server:
    port: 5080
    forward-headers-strategy: framework
~~~


