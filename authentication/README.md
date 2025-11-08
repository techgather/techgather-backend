# Authentication Module

사용자 인증(Authentication) 및 인가(Authorization) 를 담당하는 모듈입니다.  
AWS Cognito 기반의 OIDC / OAuth 2.0 로그인을 지원하며,현재는 Google 로그인을 기본 Provider로 사용합니다.  
추가 소셜 로그인은 순차적으로 확장할 예정입니다.

* 역할 
  * OIDC/OAuth2.0 인증 흐름 처리 
  * Cognito와 연동된 Access Token / ID Token 검증 
  * 로그인한 사용자 정보를 API 서버에서 공통으로 사용할 수 있는 형태로 가공
* 기본 PORT: 5080

## 실행 방법
### AWS Credentials

Cognito 로그인 및 AWS Parameter Store 값을 사용하려면 AWS CLI 자격 증명이 필요합니다.

아래 명령을 실행하여 Access Key / Secret Key를 설정해주세요.

~~~
aws configure
~~~

### 실행 환경
운영 환경 (prod)
~~~
java -jar authentication.jar --spring.profiles.active=prod
~~~

개발 환경 (dev)
~~~
java -jar authentication.jar
~~~
