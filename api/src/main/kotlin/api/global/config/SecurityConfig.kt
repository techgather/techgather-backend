package api.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { }
            .csrf { it.disable() }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/posts", "/posts/**").permitAll()
                    .requestMatchers("/api/posts", "/api/posts/**").permitAll()
                    .requestMatchers("/admin/**").permitAll()
                    .requestMatchers("/categories/**").permitAll()
                    .requestMatchers("/api/categories/**").permitAll()
                    .anyRequest().authenticated()
            }
//            .oauth2ResourceServer { oauth2 ->
//                oauth2.jwt { jwt ->
//                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
//                }
//            }
        return http.build()
    }

//    private fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
//        val grantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter().apply {
//            setAuthoritiesClaimName("cognito:groups")
//            setAuthorityPrefix("ROLE_")
//        }
//        return JwtAuthenticationConverter().apply {
//            setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter)
//        }
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:8888",
                "http://localhost:8080",
                "https://dev-pick.com"
            )
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}
