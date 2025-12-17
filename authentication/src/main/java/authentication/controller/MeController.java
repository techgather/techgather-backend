package authentication.controller;

import authentication.controller.dto.MeResponse;
import authentication.oauth.userinfo.OAuthUserInfo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class MeController {
    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal OAuthUserInfo user) {
        return MeResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .picture(user.getPicture())
                .provider(user.getAuthProvider())
                .build();
    }
}
