package com.openclassrooms.mddapi.dto;

import com.openclassrooms.mddapi.dto.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Données du formulaire d'inscription. */
public record RegisterRequest(
        @NotBlank(message = "Le nom d'utilisateur est obligatoire.")
        @Size(max = 50, message = "Le nom d'utilisateur ne doit pas dépasser 50 caractères.")
        String username,

        @NotBlank(message = "L'e-mail est obligatoire.")
        @Email(message = "L'e-mail n'est pas valide.")
        @Size(max = 255, message = "L'e-mail ne doit pas dépasser 255 caractères.")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire.")
        @ValidPassword
        String password) {

    /**
     * Retire les espaces autour du nom et de l'e-mail dès la réception, avant la
     * validation : sinon {@code @Email} refuserait « bob@mail.fr  » copié-collé
     * avec un espace. Le mot de passe est gardé tel quel.
     */
    public RegisterRequest {
        username = username == null ? null : username.strip();
        email = email == null ? null : email.strip();
    }

    /** Sans le mot de passe : un record l'afficherait sinon dans un log ou une trace. */
    @Override
    public String toString() {
        return "RegisterRequest[username=" + username + ", email=" + email + ", password=***]";
    }
}
