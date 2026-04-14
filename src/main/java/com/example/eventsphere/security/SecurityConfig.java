package com.example.eventsphere.security;

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
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Routes publiques du front
                        .requestMatchers(
                                "/auth/register",          // Visiteur (image 1)
                                "/auth/register/verified", // Visiteur vérifié (image 2)
                                "/auth/login",             // Login (image 3)
                                "/auth/forgot-password",   // Forgot password (image 4)
                                "/auth/verify-otp",        // OTP (image 5)
                                "/auth/resend-otp",        // Renvoyer OTP
                                "/auth/reset-password",    // Reset password
                                "/auth/oauth2/**",         // Google OAuth2
                                "/uploads/**"              // Fichiers uploadés
                        ).permitAll()
                        // Tout le reste requiert un JWT valide
                        .anyRequest().authenticated()
                )
                // Google OAuth2 Login
                .oauth2Login(oauth2 -> oauth2
                        .redirectionEndpoint(endpoint ->
                                endpoint.baseUri("/auth/oauth2/callback/*"))
                        .successHandler(oAuth2SuccessHandler)
                )
                // Filtre JWT avant chaque requête
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
