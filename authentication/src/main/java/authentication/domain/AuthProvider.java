package authentication.domain;

/**
 * 실제 인증을 수행한 인증 제공자
 *
 * - COGNITO: AWS Cognito가 OIDC 기반 인증을 수행
 * - UNKNOWN: 외부 provider를 확장하는 과정에서, 애플리케이션이 인지하지 못한 provider 문자열이
 *   유입될 수 있는데 이 경우 null 처리보다 UNKNOWN으로 매핑하는 것이 안전하다고 판단
 *   단, UNKNOWN의 경우 error log 기록
 *
 *  향후 추가 가능한 인증 제공자
 *  - NAVER: 네이버 로그인 (Spring OAuth2)
 *   등등
 */
public enum AuthProvider {
    COGNITO,
    UNKNOWN;

    public static AuthProvider from(String name) {
        for (AuthProvider p : values()) {
            if (p.name().equalsIgnoreCase(name)) return p;
        }
        return UNKNOWN;
    }
}
