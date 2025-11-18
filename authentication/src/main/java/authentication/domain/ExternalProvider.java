package authentication.domain;

/**
 * 사용자가 선택한 소셜 로그인 제공자
 *
 * - GOOGLE: 구글 로그인 (현재 프로젝트는 Cognito Auth Provider를 이용)
 * - UNKNOWN: 외부 provider를 확장하는 과정에서, 애플리케이션이 인지하지 못한 provider 문자열이
 *   유입될 수 있는데 이 경우 null 처리보다 UNKNOWN으로 매핑하는 것이 안전하다고 판단
 *   단, UNKNOWN의 경우 error log 기록
 *
 *  향후 추가 가능한 소셜 로그인
 * - NAVER: 네이버 로그인 (Spring OAuth2)
 * - COGNITO: Cognito 자체 계정(이메일/패스워드)로 로그인한 경우
 *  등등
 */
public enum ExternalProvider {
    UNKNOWN, GOOGLE;

    public static ExternalProvider from(String name) {
        for (ExternalProvider p : values()) {
            if (p.name().equalsIgnoreCase(name)) return p;
        }
        return UNKNOWN;
    }
}
