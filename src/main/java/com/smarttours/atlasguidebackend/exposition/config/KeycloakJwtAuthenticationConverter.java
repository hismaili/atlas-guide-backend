package com.smarttours.atlasguidebackend.exposition.config;

import java.util.List;

/**
 * Converter to extract roles from Keycloak JWT token
 */
class KeycloakJwtAuthenticationConverter implements org.springframework.core.convert.converter.Converter<
        org.springframework.security.oauth2.jwt.Jwt, 
        org.springframework.security.authentication.AbstractAuthenticationToken> {

    @Override
    public org.springframework.security.authentication.AbstractAuthenticationToken convert(org.springframework.security.oauth2.jwt.Jwt jwt) {
        // Extract roles from Keycloak token structure
        var authorities = new java.util.ArrayList<org.springframework.security.core.GrantedAuthority>();
        
        // Keycloak puts roles in realm_access.roles
        var realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            roles.forEach(role -> 
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
            );
        }
        
        // Also extract resource_access roles for specific clients
        var resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null) {
            resourceAccess.forEach((client, access) -> {
                @SuppressWarnings("unchecked")
                var clientAccess = (java.util.Map<String, Object>) access;
                if (clientAccess.containsKey("roles")) {
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) clientAccess.get("roles");
                    roles.forEach(role -> 
                        authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                    );
                }
            });
        }

        return new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(
            jwt, 
            authorities
        );
    }
}