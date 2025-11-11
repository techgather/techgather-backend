package authentication.config;

import authentication.service.CustomOidcUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOidcSuccessHandler oidcSuccessHandler;
    private final CustomOidcUserService customOidcUserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable()) // h2 콘솔 접근 시 CSRF 끄기
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable) // H2 콘솔은 iframe 사용
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/logout", "/login/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll() // h2 콘솔만.
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(u -> u.oidcUserService(customOidcUserService))
                        .successHandler(oidcSuccessHandler)
                )
                .build();
    }
}
