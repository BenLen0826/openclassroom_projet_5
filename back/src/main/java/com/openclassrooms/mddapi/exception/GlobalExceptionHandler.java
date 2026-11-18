package com.openclassrooms.mddapi.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Transforme les exceptions en réponses {@link ProblemDetail} (RFC 9457).
 * <p>
 * Hérite de {@link ResponseEntityExceptionHandler} : les erreurs standard de Spring
 * MVC gardent leur code ({@code 400} corps illisible, {@code 404}, {@code 405},
 * {@code 415}…) au lieu de tomber dans le cas « erreur inattendue ».
 * <p>
 * Le {@code 401} lié au token n'est pas traité ici : il est renvoyé par le module
 * OAuth2 Resource Server, avant d'arriver aux contrôleurs.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** {@code 400} : la liste des erreurs par champ, pour les afficher sous les champs. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            // Plusieurs erreurs sur un même champ : seule la première est affichée
            errors.putIfAbsent(error.getField(),
                    error.getDefaultMessage() != null ? error.getDefaultMessage() : "Valeur invalide.");
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, "Un ou plusieurs champs sont invalides.");
        problem.setTitle("Données invalides");
        problem.setProperty("errors", errors);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Conflit");
        if (ex.getField() != null) {
            problem.setProperty("field", ex.getField());
        }
        return problem;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("Authentification refusée");
        return problem;
    }

    /** Toute autre erreur : message générique, détail technique uniquement dans le log. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Erreur inattendue", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur inattendue est survenue.");
        problem.setTitle("Erreur serveur");
        return problem;
    }
}
