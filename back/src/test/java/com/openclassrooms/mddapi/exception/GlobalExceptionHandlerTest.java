package com.openclassrooms.mddapi.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleConflict_returns409WithMessageAndField() {
        // Act
        ProblemDetail problem = handler.handleConflict(new EmailAlreadyUsedException());

        // Assert
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getDetail()).isEqualTo("Cet e-mail est déjà utilisé.");
        assertThat(problem.getProperties()).containsEntry("field", "email");
    }

    @Test
    void handleConflict_withoutField_hasNoFieldProperty() {
        // Act
        ProblemDetail problem = handler.handleConflict(new AccountAlreadyExistsException());

        // Assert
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getDetail()).isEqualTo("Cet e-mail ou ce nom d'utilisateur est déjà utilisé.");
        assertThat(problem.getProperties()).isNullOrEmpty();
    }

    @Test
    void handleInvalidCredentials_returns401WithGenericMessage() {
        // Act
        ProblemDetail problem = handler.handleInvalidCredentials(new InvalidCredentialsException());

        // Assert
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(problem.getTitle()).isEqualTo("Authentification refusée");
        assertThat(problem.getDetail()).isEqualTo("Identifiant ou mot de passe incorrect.");
    }

    @Test
    void handleUnknownUser_returns401() {
        // Act
        ProblemDetail problem = handler.handleUnknownUser(new UnknownUserException());

        // Assert
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(problem.getDetail()).isEqualTo("Session invalide. Reconnectez-vous.");
    }

    @Test
    void handleTopicNotFound_returns404() {
        // Act
        ProblemDetail problem = handler.handleTopicNotFound(new TopicNotFoundException());

        // Assert
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getDetail()).isEqualTo("Thème introuvable.");
    }

    @Test
    void handleDuplicateSubscription_returns204() {
        // L'abonnement existe déjà (requête simultanée) : le résultat voulu est atteint
        assertThat(handler.handleDuplicateSubscription(new DuplicateSubscriptionException()).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void handleUnexpected_returnsGenericMessageWithoutInternalDetail() {
        // Act
        ProblemDetail problem = handler.handleUnexpected(new RuntimeException("boom"));

        // Assert
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getDetail()).isEqualTo("Une erreur inattendue est survenue.");
        assertThat(problem.toString()).doesNotContain("boom");
    }
}
