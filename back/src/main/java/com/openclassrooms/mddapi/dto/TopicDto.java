package com.openclassrooms.mddapi.dto;

/**
 * Thème renvoyé au front.
 * <p>
 * Le champ {@code subscribed} (état d'abonnement de l'utilisateur connecté)
 * sera ajouté à l'étape 5, avec l'authentification.
 */
public record TopicDto(Long id, String name, String description) {
}
