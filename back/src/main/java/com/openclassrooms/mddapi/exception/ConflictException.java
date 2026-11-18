package com.openclassrooms.mddapi.exception;

import lombok.Getter;

/**
 * Conflit avec une donnée existante, renvoyé en {@code 409}.
 * <p>
 * {@code field} indique le champ du formulaire concerné, pour que le front affiche
 * le message sous ce champ ; {@code null} si aucun champ précis n'est en cause.
 */
@Getter
public abstract class ConflictException extends RuntimeException {

    private final String field;

    protected ConflictException(String message, String field) {
        super(message);
        this.field = field;
    }
}
