package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.dto.TopicDto;
import com.openclassrooms.mddapi.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    /** L'utilisateur est déduit du token (sujet = id), jamais d'un paramètre de la requête. */
    @GetMapping
    public List<TopicDto> getAllTopics(@AuthenticationPrincipal Jwt jwt) {
        return topicService.getAllTopics(Long.valueOf(jwt.getSubject()));
    }
}
