package com.openclassrooms.mddapi.security;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Utilisateur connecté, lu dans le token JWT vérifié par Spring Security.
 * <p>
 * L'identifiant vient toujours du sujet ({@code sub}) du token, jamais de l'URL ni du corps de la
 * requête : un utilisateur ne peut donc agir qu'en son propre nom (profil, abonnements, auteur
 * d'un article ou d'un commentaire).
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    /**
     * Identifiant de l'utilisateur connecté.
     *
     * @param jwt token de la requête, injecté par {@code @AuthenticationPrincipal}
     * @return l'identifiant de l'utilisateur, sujet du token
     */
    public static Long id(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
