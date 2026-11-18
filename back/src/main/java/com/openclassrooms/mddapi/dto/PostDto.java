package com.openclassrooms.mddapi.dto;

import java.time.Instant;

/** Article renvoyé au front (fil, création, page d'un article). */
public record PostDto(
        Long id,
        String title,
        String content,
        Instant createdAt,
        String authorUsername,
        Long topicId,
        String topicName) {
}
