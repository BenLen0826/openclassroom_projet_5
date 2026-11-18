package com.openclassrooms.mddapi.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de Spring Security : API sans session, authentifiée par JWT.
 * <p>
 * <ul>
 *   <li>CSRF désactivé : le token est envoyé dans l'en-tête {@code Authorization},
 *       jamais dans un cookie, donc un site tiers ne peut pas l'utiliser à la place
 *       de l'utilisateur.</li>
 *   <li>Aucune session HTTP : chaque requête porte son token.</li>
 *   <li>OAuth2 Resource Server vérifie le token (signature HS256, expiration) ; un
 *       token absent sur une route protégée, invalide ou expiré donne {@code 401}.</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // utilise le bean CorsConfigurationSource
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Renvoi interne vers /error : sans cette règle, une erreur serveur (500)
                        // serait masquée par un 401, faute d'utilisateur authentifié.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
