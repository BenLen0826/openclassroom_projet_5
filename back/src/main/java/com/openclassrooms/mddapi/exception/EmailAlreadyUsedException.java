package com.openclassrooms.mddapi.exception;

/** E-mail déjà utilisé par un autre compte ({@code 409}, message affiché sous le champ « email »). */
public class EmailAlreadyUsedException extends ConflictException {

    public EmailAlreadyUsedException() {
        super("Cet e-mail est déjà utilisé.", "email");
    }
}
