package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Abonnements de l'utilisateur connecté (« me » : déduit du token, jamais de l'URL). */
@RestController
@RequestMapping("/api/users/me/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/{topicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subscribe(@AuthenticationPrincipal Jwt jwt, @PathVariable Long topicId) {
        subscriptionService.subscribe(Long.valueOf(jwt.getSubject()), topicId);
    }

    @DeleteMapping("/{topicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@AuthenticationPrincipal Jwt jwt, @PathVariable Long topicId) {
        subscriptionService.unsubscribe(Long.valueOf(jwt.getSubject()), topicId);
    }
}
