package com.smarttours.atlasguidebackend.exposition.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration for Tours Advisor API
 * Integrates with Keycloak for OAuth2/JWT authentication
 */
@Configuration
public class SecurityConfig {

    @Value("${app.security.dev-mode:false}")
    private boolean devMode;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF not needed for stateless JWT authentication
            .csrf(AbstractHttpConfigurer::disable)
            
            // Stateless session management
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/api/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/error", "/swagger-ui**").permitAll()
                
                // Admin endpoints - require ADMIN role
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // OAuth2 Resource Server with JWT
            .oauth2ResourceServer(oauth2 -> 
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter()))
            );

        return http.build();
    }

    /**
     * CORS configuration for React Native mobile app
     */
    @Bean
    @Profile("!bff")
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        if (devMode) {
            configuration.setAllowedOriginPatterns(List.of("*"));
            configuration.setAllowCredentials(true);
        } else {
            configuration.setAllowedOrigins(List.of(
                    "https://toursadvisor.ddnsgeek.com",
                    "https://api.toursadvisor.ddnsgeek.com"
            ));
        }

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}