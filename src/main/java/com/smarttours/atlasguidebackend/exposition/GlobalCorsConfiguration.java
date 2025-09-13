package com.smarttours.atlasguidebackend.exposition;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class GlobalCorsConfiguration {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // In production, you should be more restrictive than "*".
        // For example: config.setAllowedOrigins(Arrays.asList("https://your-frontend-domain.com"));
        config.addAllowedOrigin("*"); 
        
        config.setAllowCredentials(false); // Set to true if you need cookies/auth headers
        config.addAllowedHeader("*");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}