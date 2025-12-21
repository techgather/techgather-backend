package authentication.config;

import authentication.oauth.handler.OAuth2LoginFailureHandler;
import authentication.oauth.handler.OAuth2LoginSuccessHandler;
import authentication.oauth.handler.OAuth2LogoutSuccessHandler;
import authentication.oauth.service.OidcUserSyncService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OidcUserSyncService customOidcUserService;
    private final OAuth2LoginFailureHandler failureHandler;
    private final OAuth2LogoutSuccessHandler logoutSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login/**",
                                "/logout/**",
                                "/refresh")
                        .permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/.well-known/jwks.json").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(u -> u.oidcUserService(customOidcUserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(failureHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .logoutSuccessUrl("/")
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, authEx) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"message\": \"Unauthorized\"}");
                        })
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .build();
    }
}
