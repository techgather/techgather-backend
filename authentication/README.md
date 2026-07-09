# Authentication Module

사용자 인증(Authentication) 및 인가(Authorization)를 담당할 모듈입니다.
현재 외부 인증 연동은 제거되어 있으며, 추후 직접 OAuth 연동을 추가할 예정입니다.

* 역할 
  * 인증 서버 엔트리포인트 유지
  * 추후 OAuth 인증 흐름 추가
  * 로그인한 사용자 정보를 API 서버에서 공통으로 사용할 수 있는 형태로 가공 예정
* 기본 PORT: 5080

## 실행 방법
### 실행 환경
운영 환경 (prod)
~~~
java -jar authentication.jar --spring.profiles.active=prod
~~~

개발 환경 (dev)
~~~
java -jar authentication.jar
~~~
