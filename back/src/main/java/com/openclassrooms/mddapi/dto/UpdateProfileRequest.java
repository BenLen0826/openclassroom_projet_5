package com.openclassrooms.mddapi.dto;

import com.openclassrooms.mddapi.dto.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Données du formulaire du profil. Mêmes règles que l'inscription pour le nom et
 * l'e-mail ; le mot de passe est facultatif : absent ou vide, il ne change pas.
 */
public record UpdateProfileRequest(
        @NotBlank(message = "Le nom d'utilisateur est obligatoire.")
        @Size(max = 50, message = "Le nom d'utilisateur ne doit pas dépasser 50 caractères.")
        String username,

        @NotBlank(message = "L'e-mail est obligatoire.")
        @Email(message = "L'e-mail n'est pas valide.")
        @Size(max = 255, message = "L'e-mail ne doit pas dépasser 255 caractères.")
        String email,

        @ValidPassword
        String password) {

    /**
     * Retire les espaces autour du nom et de l'e-mail ; un mot de passe vide ou fait
     * d'espaces devient {@code null} (« pas de changement ») avant la validation.
     */
    public UpdateProfileRequest {
        username = username == null ? null : username.strip();
        email = email == null ? null : email.strip();
        password = password == null || password.isBlank() ? null : password;
    }

    /** Sans le mot de passe : un record l'afficherait sinon dans un log ou une trace. */
    @Override
    public String toString() {
        return "UpdateProfileRequest[username=" + username + ", email=" + email + ", password=***]";
    }
}
