package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Données du formulaire de connexion.
 * <p>
 * Pas de règle de mot de passe ici : seule l'inscription (et plus tard la
 * modification du profil) l'impose.
 *
 * @param identifier e-mail ou nom d'utilisateur
 */
public record LoginRequest(
        @NotBlank(message = "L'identifiant est obligatoire.")
        String identifier,

        @NotBlank(message = "Le mot de passe est obligatoire.")
        String password) {

    /** Retire les espaces autour de l'identifiant ; le mot de passe est gardé tel quel. */
    public LoginRequest {
        identifier = identifier == null ? null : identifier.strip();
    }

    /** Sans le mot de passe : un record l'afficherait sinon dans un log ou une trace. */
    @Override
    public String toString() {
        return "LoginRequest[identifier=" + identifier + ", password=***]";
    }
}
