package com.smarttours.atlasguidebackend.utils;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WithMockJwtAuthSecurityContextFactory implements WithSecurityContextFactory<WithMockJwtAuth> {
    
    @Override
    public SecurityContext createSecurityContext(WithMockJwtAuth annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        
        // Build JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", annotation.subject());
        
        // Parse additional claims if provided
        for (String claim : annotation.claims()) {
            String[] parts = claim.split(":");
            if (parts.length == 2) {
                claims.put(parts[0], parts[1]);
            }
        }
        
        // Create authorities
        List<GrantedAuthority> authorities = Arrays.stream(annotation.authorities())
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        
        // Create JWT
        Jwt jwt = Jwt.withTokenValue("mock-token")
            .header("alg", "none")
            .claims(c -> c.putAll(claims))
            .build();
        
        // Create authentication token
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, authorities);
        context.setAuthentication(auth);
        
        return context;
    }
}