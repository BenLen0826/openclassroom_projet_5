package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.dto.AuthResponse;
import com.openclassrooms.mddapi.dto.LoginRequest;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inscription et connexion : les deux seules routes métier publiques (sans token).
 * <p>
 * {@code @SecurityRequirements} vide : Swagger n'y envoie pas le token, un token périmé saisi
 * dans « Authorize » ferait sinon refuser la connexion.
 */
@SecurityRequirements
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Inscription : l'utilisateur est connecté directement (token renvoyé). */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /** Connexion par e-mail ou nom d'utilisateur. */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
