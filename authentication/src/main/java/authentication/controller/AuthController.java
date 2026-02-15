package authentication.controller;

import application.exception.CommonClientErrorCode;
import application.exception.UnAuthorizedException;
import authentication.dto.principal.AuthenticatedUser;
import authentication.dto.response.OAuthTokenResponse;
import authentication.oauth.userinfo.CustomOAuthUserInfo;
import authentication.service.AuthService;
import authentication.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    public AuthenticatedUser getMe(@AuthenticationPrincipal AuthenticatedUser user){
        return user;
    }

    @PostMapping("/refresh")
    public CustomOAuthUserInfo refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = cookieUtil.extractRefreshToken(request);

        if (refreshToken == null) {
            throw new UnAuthorizedException(CommonClientErrorCode.UNAUTHORIZED, null);
        }

        OAuthTokenResponse tokenResponse = authService.refreshToken(refreshToken);

        response.setHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + tokenResponse.accessToken()
        );

        if (tokenResponse.refreshToken() != null) {
            cookieUtil.setRefreshTokenCookie(
                    response,
                    tokenResponse.refreshToken()
            );
        }

        return tokenResponse.userInfo();
    }

    @PostMapping("/logout")
    public String logout(@AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request, HttpServletResponse res){
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String userName = user.sub();
        if (userName == null) {
            throw new IllegalArgumentException("User name is null.");
        }

        String logoutUrl = authService.logout(user.sub(), baseUrl);
        
        cookieUtil.deleteRefreshTokenCookie(res);

        return logoutUrl;
    }
}
