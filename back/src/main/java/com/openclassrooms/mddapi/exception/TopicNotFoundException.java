package com.openclassrooms.mddapi.exception;

/** Thème demandé inexistant, renvoyé en {@code 404}. */
public class TopicNotFoundException extends RuntimeException {

    public TopicNotFoundException() {
        super("Thème introuvable.");
    }
}
