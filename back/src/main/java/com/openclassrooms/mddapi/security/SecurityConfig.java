package com.openclassrooms.mddapi.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de Spring Security.
 * <p>
 * Étape 4 (strict minimum) : Spring Security verrouille tout par défaut. On ouvre
 * uniquement la liste des thèmes et on active le CORS pour que le front puisse
 * l'appeler. L'authentification JWT arrive à l'étape 5.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // utilise le bean CorsConfigurationSource
                .authorizeHttpRequests(auth -> auth
                        // Renvoi interne vers /error : sans cette règle, une erreur serveur (500)
                        // serait masquée par un 403 vide, faute d'utilisateur authentifié.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        // TEMPORAIRE (étape 4) : passera sous JWT à l'étape 5
                        .requestMatchers(HttpMethod.GET, "/api/topics").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
