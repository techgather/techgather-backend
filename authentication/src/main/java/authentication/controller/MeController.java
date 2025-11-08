package authentication.controller;

import authentication.dto.MeResponse;
import authentication.userinfo.CustomOAuthUserInfo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class MeController {
    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal CustomOAuthUserInfo user) {
        return MeResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .picture(user.getPicture())
                .provider(user.getAuthProvider())
                .build();
    }
}
