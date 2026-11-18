package com.openclassrooms.mddapi.dto;

/** Profil de l'utilisateur connecté (jamais de mot de passe, même haché). */
public record UserDto(Long id, String username, String email) {
}
