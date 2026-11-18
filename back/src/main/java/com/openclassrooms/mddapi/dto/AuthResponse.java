package com.openclassrooms.mddapi.dto;

/** Réponse de l'inscription et de la connexion : le JWT à envoyer ensuite. */
public record AuthResponse(String token) {
}
