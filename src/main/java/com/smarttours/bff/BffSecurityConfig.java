package com.smarttours.bff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttours.atlasguidebackend.exposition.config.KeycloakJwtAuthenticationConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

@Configuration
@Profile("bff")
public class BffSecurityConfig {

    private static final Logger LOG = LoggerFactory.getLogger(BffSecurityConfig.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain bffFilterChain(HttpSecurity http,
                                              ClientRegistrationRepository clients) throws Exception {

        var logoutHandler = new OidcClientInitiatedLogoutSuccessHandler(clients);
        logoutHandler.setPostLogoutRedirectUri(frontendUrl + "/auth/login");

        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/oauth2/**", "/login/**", "/auth/user").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers("/api/health", "/api/public/**", "/actuator/health").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                // Session-based login for browser clients (frontend, Swagger via session)
                .oauth2Login(login -> login
                        .defaultSuccessUrl(frontendUrl + "/auth/callback", true)
                        // Return the actual error as JSON — prevents the /login?error 404
                        .failureHandler((request, response, exception) -> {
                            LOG.error("OAuth2 login failed: {}", exception.getMessage(), exception);
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(OBJECT_MAPPER.writeValueAsString(
                                    Map.of("error", "login_failed", "message", exception.getMessage())
                            ));
                        })
                )
                // Bearer JWT for any API client (mobile, curl, Swagger Authorize button)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter()))
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"))
                        .logoutSuccessHandler(logoutHandler)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                // Return 401 for unauthenticated requests — clients decide how to authenticate
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(bffCorsConfigurationSource()))
                .build();
    }

    @Bean
    public CorsConfigurationSource bffCorsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendUrl));
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
