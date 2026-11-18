package com.openclassrooms.mddapi.exception;

public class EmailAlreadyUsedException extends ConflictException {

    public EmailAlreadyUsedException() {
        super("Cet e-mail est déjà utilisé.", "email");
    }
}
