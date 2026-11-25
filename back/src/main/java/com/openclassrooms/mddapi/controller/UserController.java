package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.dto.UpdateProfileRequest;
import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.security.CurrentUser;
import com.openclassrooms.mddapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Profil de l'utilisateur connecté (« me » : déduit du token, jamais de l'URL). */
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Profil de l'utilisateur connecté (id, nom et e-mail, jamais le mot de passe). */
    @GetMapping
    public UserDto getMe(@AuthenticationPrincipal Jwt jwt) {
        return userService.getMe(CurrentUser.id(jwt));
    }

    /**
     * Modification du profil ; mot de passe vide = inchangé. {@code 400} si un champ est invalide,
     * {@code 409} si le nom ou l'e-mail est déjà pris par un autre compte.
     */
    @PutMapping
    public UserDto updateMe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateMe(CurrentUser.id(jwt), request);
    }
}
