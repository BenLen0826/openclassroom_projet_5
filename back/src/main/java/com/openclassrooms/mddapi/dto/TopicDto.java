package com.openclassrooms.mddapi.dto;

/**
 * Thème renvoyé au front.
 *
 * @param subscribed {@code true} si l'utilisateur connecté suit ce thème
 */
public record TopicDto(Long id, String name, String description, boolean subscribed) {
}
