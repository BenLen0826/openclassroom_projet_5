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

    /**
     * {@code 409} : e-mail ou nom d'utilisateur déjà pris. Le champ concerné ({@code field}) est
     * ajouté à la réponse, pour afficher le message sous le bon champ.
     *
     * @param ex conflit, avec le champ concerné s'il est connu
     * @return l'erreur au format ProblemDetail
     */
    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Conflit");
        if (ex.getField() != null) {
            problem.setProperty("field", ex.getField());
        }
        return problem;
    }

    /**
     * {@code 401} : identifiants refusés à la connexion, ou token valide d'un compte qui n'existe
     * plus. Même titre dans les deux cas ; le message vient de l'exception.
     *
     * @param ex identifiants refusés ou compte inconnu
     * @return l'erreur au format ProblemDetail
     */
    @ExceptionHandler({InvalidCredentialsException.class, UnknownUserException.class})
    public ProblemDetail handleUnauthorized(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("Authentification refusée");
        return problem;
    }

    /**
     * {@code 404} : thème ou article introuvable (ex. supprimé, ou identifiant inexistant).
     *
     * @param ex thème ou article introuvable
     * @return l'erreur au format ProblemDetail
     */
    @ExceptionHandler({TopicNotFoundException.class, PostNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Ressource introuvable");
        return problem;
    }

    /** Abonnement simultané en double : l'abonnement existe, la demande est satisfaite. */
    @ExceptionHandler(DuplicateSubscriptionException.class)
    public ResponseEntity<Void> handleDuplicateSubscription(DuplicateSubscriptionException ex) {
        return ResponseEntity.noContent().build();
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
