package authentication.config;

import authentication.service.CustomOidcUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOidcSuccessHandler oidcSuccessHandler;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2LoginFailureHandler failureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable()) // h2 콘솔 접근 시 CSRF 끄기
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .oauth2Login(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable) // H2 콘솔은 iframe 사용
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/",
                                "/auth/me",
                                "/auth/login/**",
                                "/auth/logout/**")
                        .permitAll()
                        .requestMatchers("/h2-console/**").permitAll() // h2 콘솔만 임시
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(u -> u.oidcUserService(customOidcUserService))
                        .successHandler(oidcSuccessHandler)
                        .failureHandler(failureHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")// 향후? 프론트에서 호출할 URL, stateless 시 제거
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")      // 브라우저 쿠키까지 제거
                        .logoutSuccessUrl("/auth/me")
                )
                .build();
    }

}
