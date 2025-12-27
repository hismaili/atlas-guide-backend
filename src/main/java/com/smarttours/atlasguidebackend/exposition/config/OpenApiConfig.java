package com.smarttours.atlasguidebackend.exposition.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${springdoc.swagger-ui.oauth-config.authorization-url}")
    private String authorizationUrl;

    @Value("${springdoc.swagger-ui.oauth-config.token-url}")
    private String tokenUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("keycloak", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("Keycloak Authentication")
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl(authorizationUrl) // Injected from config
                                                .tokenUrl(tokenUrl)                 // Injected from config
                                                .scopes(new Scopes().addString("openid", "openid scope"))))))
                .addSecurityItem(new SecurityRequirement().addList("keycloak"));
    }
}