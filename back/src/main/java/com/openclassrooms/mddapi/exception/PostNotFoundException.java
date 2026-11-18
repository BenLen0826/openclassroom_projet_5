package com.openclassrooms.mddapi.exception;

/** Article demandé inexistant, renvoyé en {@code 404}. */
public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException() {
        super("Article introuvable.");
    }
}
