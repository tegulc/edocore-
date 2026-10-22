package com.educore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI educoreOpenApi() {
        final String schemeJwt = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("EDUCORE / SIGES API")
                        .description("API REST du systeme ERP intelligent de gestion d'etablissements scolaires")
                        .version("v1 - Phase 0"))
                .addSecurityItem(new SecurityRequirement().addList(schemeJwt))
                .components(new Components().addSecuritySchemes(schemeJwt,
                        new SecurityScheme()
                                .name(schemeJwt)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
