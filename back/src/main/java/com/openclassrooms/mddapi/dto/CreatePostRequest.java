package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Données du formulaire de création d'un article. L'auteur et la date n'en font pas
 * partie : ils sont définis par le serveur (champs inconnus ignorés par Jackson).
 */
public record CreatePostRequest(
        @NotNull(message = "Le thème est obligatoire.")
        Long topicId,

        @NotBlank(message = "Le titre est obligatoire.")
        @Size(max = 255, message = "Le titre ne doit pas dépasser 255 caractères.")
        String title,

        @NotBlank(message = "Le contenu est obligatoire.")
        // Colonne TEXT de MySQL : 65 535 octets ; 20 000 caractères y tiennent toujours,
        // même en caractères de 4 octets (emojis). Au-delà : 400 au lieu d'une erreur 500.
        @Size(max = 20_000, message = "Le contenu ne doit pas dépasser 20 000 caractères.")
        String content) {

    /** Retire les espaces autour du titre ; le contenu est gardé tel quel (mise en page). */
    public CreatePostRequest {
        title = title == null ? null : title.strip();
    }
}
