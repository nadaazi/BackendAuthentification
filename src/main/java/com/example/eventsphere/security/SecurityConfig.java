package com.example.eventsphere.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired private JwtAuthFilter jwtAuthFilter;
    @Autowired private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/auth/register",
                    "/auth/register/verified",
                    "/auth/login",
                    "/auth/forgot-password",
                    "/auth/verify-otp",
                    "/auth/resend-otp",
                    "/auth/reset-password",
                    "/auth/google-token",
                    "/auth/test-email",
                    "/auth/oauth2/**",
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/uploads/**",
                    "/error"
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers(
                    org.springframework.http.HttpMethod.GET,
                    "/events", "/events/*", "/events/search",
                    "/events/categories", "/events/upcoming", "/events/free",
                    "/events/organizer/*", "/tickettypes/event/*",
                    "/localisations"
                ).permitAll()
                .requestMatchers("/events/**", "/tickettypes/**").authenticated()
                .requestMatchers("/billets/**").authenticated()
                .requestMatchers("/localisations/**").authenticated()
                .requestMatchers("/stats/**").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write(new ObjectMapper().writeValueAsString(
                        Map.of("success", false, "message", "Token manquant ou invalide. Veuillez vous connecter.", "data", "")
                    ));
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write(new ObjectMapper().writeValueAsString(
                        Map.of("success", false, "message", "Accès refusé. Droits insuffisants.", "data", "")
                    ));
                })
            )
            .oauth2Login(oauth2 -> oauth2
                .redirectionEndpoint(endpoint ->
                    endpoint.baseUri("/login/oauth2/callback/*"))
                .successHandler(oAuth2SuccessHandler)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
