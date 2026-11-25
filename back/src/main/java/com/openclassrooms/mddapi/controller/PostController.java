package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.dto.CreatePostRequest;
import com.openclassrooms.mddapi.dto.PostDto;
import com.openclassrooms.mddapi.security.CurrentUser;
import com.openclassrooms.mddapi.service.PostService;
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

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /** Fil d'actualité de l'utilisateur connecté, du plus récent au plus ancien. */
    @GetMapping
    public List<PostDto> getFeed(@AuthenticationPrincipal Jwt jwt) {
        return postService.getFeed(CurrentUser.id(jwt));
    }

    /** Publication : l'auteur est déduit du token, la date est mise par le serveur. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDto create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreatePostRequest request) {
        return postService.create(CurrentUser.id(jwt), request);
    }

    @GetMapping("/{id}")
    public PostDto getById(@PathVariable Long id) {
        return postService.getById(id);
    }
}
