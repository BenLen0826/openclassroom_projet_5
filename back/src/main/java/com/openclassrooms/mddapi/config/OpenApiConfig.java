package com.openclassrooms.mddapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Documentation Swagger : déclare l'authentification par JWT, ce qui ajoute le
 * bouton « Authorize » dans Swagger UI pour essayer les routes protégées.
 * Les routes publiques (inscription, connexion) fonctionnent sans token.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "MDD API", version = "v1"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}
