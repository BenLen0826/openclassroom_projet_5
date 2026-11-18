package com.openclassrooms.mddapi.exception;

/**
 * Échec de connexion, renvoyé en {@code 401}.
 * <p>
 * Même message que le compte soit inconnu ou que le mot de passe soit faux : la
 * réponse ne doit pas révéler si un e-mail ou un nom d'utilisateur existe.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Identifiant ou mot de passe incorrect.");
    }
}
