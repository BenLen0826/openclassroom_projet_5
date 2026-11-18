package com.openclassrooms.mddapi.exception;

/**
 * Le token est valide mais son utilisateur n'existe plus en base : renvoyé en
 * {@code 401}, pour que le front déconnecte l'utilisateur.
 */
public class UnknownUserException extends RuntimeException {

    public UnknownUserException() {
        super("Session invalide. Reconnectez-vous.");
    }
}
