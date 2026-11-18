package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Nouveau commentaire : seulement le contenu ; l'auteur et la date sont définis par le serveur. */
public record CreateCommentRequest(
        @NotBlank(message = "Le commentaire est obligatoire.")
        // Même limite que les articles : colonne TEXT de MySQL (65 535 octets)
        @Size(max = 20_000, message = "Le commentaire ne doit pas dépasser 20 000 caractères.")
        String content) {
}
