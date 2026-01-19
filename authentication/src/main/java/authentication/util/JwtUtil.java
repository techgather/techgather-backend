package authentication.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtDecoder jwtDecoder;

    /**
     * Access Token에서 client_id를 추출합니다.
     * @param token 헤더에서 추출한 String 형태의 Access Token
     * @return 추출된 client_id
     * @throws JwtException 토큰이 유효하지 않거나 파싱에 실패할 경우 발생
     */
    public String extractClientId(String token) {
        try {
            // 1. 토큰 해독 및 유효성 검증 (서명, 만료시간 등)
            Jwt jwt = jwtDecoder.decode(token);

            // 2. client_id 클레임 추출
            return jwt.getClaimAsString("client_id");

        } catch (JwtException e) {
            // 토큰이 변조되었거나 만료된 경우 예외 발생
            throw new IllegalArgumentException("유효하지 않은 토큰입니다: " + e.getMessage());
        }
    }

    /**
     * 필요 시 다른 클레임(sub, email 등)도 추출할 수 있도록 확장 가능합니다.
     */
    public String extractClaim(String token, String claimName) {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getClaimAsString(claimName);
    }
}
