package com.openclassrooms.mddapi.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Réglages du JWT ({@code mdd.jwt.*}), vérifiés au démarrage : l'API refuse de
 * démarrer sans clé, ou avec une clé de moins de 32 caractères (256 bits, minimum
 * pour HS256).
 *
 * @param secret     clé de signature HS256, lue dans la variable {@code JWT_SECRET}
 * @param expiration durée de validité d'un token (7 jours)
 */
@ConfigurationProperties("mdd.jwt")
@Validated
public record JwtProperties(
        @NotBlank @Size(min = 32) String secret,
        @NotNull Duration expiration) {

    /** Jamais la clé dans un log ou une trace. */
    @Override
    public String toString() {
        return "JwtProperties[secret=***, expiration=" + expiration + "]";
    }
}
