package com.openclassrooms.mddapi.exception;

/** Nom d'utilisateur déjà utilisé par un autre compte ({@code 409}, message affiché sous le champ « username »). */
public class UsernameAlreadyUsedException extends ConflictException {

    public UsernameAlreadyUsedException() {
        super("Ce nom d'utilisateur est déjà utilisé.", "username");
    }
}
