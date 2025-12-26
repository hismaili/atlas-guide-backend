package com.smarttours.atlasguidebackend.exposition.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("keycloak", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("Keycloak Authentication")
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                // These will be populated by the SPRINGDOC_ properties
                                                .authorizationUrl("") 
                                                .tokenUrl("")
                                                .scopes(new Scopes().addString("openid", "openid scope"))))))
                .addSecurityItem(new SecurityRequirement().addList("keycloak"));
    }
}