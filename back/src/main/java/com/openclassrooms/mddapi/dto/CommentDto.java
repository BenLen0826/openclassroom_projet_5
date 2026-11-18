package com.openclassrooms.mddapi.dto;

import java.time.Instant;

/** Commentaire renvoyé au front. */
public record CommentDto(Long id, String content, Instant createdAt, String authorUsername) {
}
