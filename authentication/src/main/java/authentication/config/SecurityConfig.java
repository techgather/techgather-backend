package authentication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler("/me");

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index", "/logout").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(successHandler)
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())); // JWT 인증 활성화
        return http.build();
    }
}
