package com.openclassrooms.mddapi.exception;

public class UsernameAlreadyUsedException extends ConflictException {

    public UsernameAlreadyUsedException() {
        super("Ce nom d'utilisateur est déjà utilisé.", "username");
    }
}
