package com.smarttours.bff;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@Profile("bff")
public class BffSecurityConfig {

    public static final String JSESSIONID = "JSESSIONID";
    @Value("${app.frontend-url}")
    private String frontendUrl;
    @Value("${app.security.dev-mode:false}")
    private boolean devMode;

    @Bean
    @Order(1)
    public SecurityFilterChain clientSecFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/oauth2/**", "/login/**", "/auth/**", "/api/proxy/**",
                    "/swagger-ui/**", "/api/health", "/api/public/**", "/api/admin/**")
                .authorizeHttpRequests(
auth -> auth
                                    .requestMatchers("/oauth2/**", "/login/**",
                                    "/swagger-ui/**", "/v3/api-docs/**", "/error", "/swagger-ui**",
                                    "/api/health", "/api/public/**",
                                    "/actuator/health"
                                    ).permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated()
                )
                .oauth2Login(login -> login.defaultSuccessUrl(frontendUrl+"/auth/callback", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl(frontendUrl+"/auth/login")
                        .invalidateHttpSession(true)
                        .deleteCookies(JSESSIONID)
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .build();
    }

    @Bean
    @Profile("bff")
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        if (devMode) {
            configuration.setAllowedOriginPatterns(List.of("*"));
            configuration.setAllowCredentials(true);
        } else {
            configuration.setAllowedOrigins(List.of(frontendUrl));
        }
            configuration.setAllowCredentials(true);          // REQUIRED: allows cookies cross-origin
            configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(List.of("*"));
            configuration.setExposedHeaders(List.of("Authorization", "X-XSRF-TOKEN"));
            configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
