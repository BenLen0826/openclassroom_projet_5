package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.dto.TopicDto;
import com.openclassrooms.mddapi.security.CurrentUser;
import com.openclassrooms.mddapi.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Thèmes : la liste complète, avec l'état d'abonnement de l'utilisateur connecté. */
@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    /**
     * Tous les thèmes, chacun avec {@code subscribed} pour l'utilisateur connecté (déduit du token,
     * jamais d'un paramètre de la requête).
     */
    @GetMapping
    public List<TopicDto> getAllTopics(@AuthenticationPrincipal Jwt jwt) {
        return topicService.getAllTopics(CurrentUser.id(jwt));
    }
}
