package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.dto.CommentDto;
import com.openclassrooms.mddapi.dto.CreateCommentRequest;
import com.openclassrooms.mddapi.security.CurrentUser;
import com.openclassrooms.mddapi.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Commentaires d'un article : endpoint séparé, seule la liste est rechargée après un ajout. */
@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** Commentaires de l'article, du plus ancien au plus récent ; {@code 404} si l'article n'existe pas. */
    @GetMapping
    public List<CommentDto> getComments(@PathVariable Long postId) {
        return commentService.getForPost(postId);
    }

    /** Ajout d'un commentaire : l'auteur est déduit du token, la date est mise par le serveur. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@AuthenticationPrincipal Jwt jwt, @PathVariable Long postId,
                             @Valid @RequestBody CreateCommentRequest request) {
        return commentService.create(CurrentUser.id(jwt), postId, request);
    }
}
