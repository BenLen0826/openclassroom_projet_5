package com.openclassrooms.mddapi.exception;

/**
 * Deux inscriptions identiques simultanées : la contrainte d'unicité de la base
 * a refusé la seconde. On ne sait pas quel champ est en cause.
 */
public class AccountAlreadyExistsException extends ConflictException {

    public AccountAlreadyExistsException() {
        super("Cet e-mail ou ce nom d'utilisateur est déjà utilisé.", null);
    }
}
