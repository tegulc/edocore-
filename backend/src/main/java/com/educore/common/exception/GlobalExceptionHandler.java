package com.educore.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Gestionnaire global des exceptions. Convertit toute erreur levee par un
 * controller/service en reponse {@link ApiError} conforme au Volume 6
 * (Documentation API), Partie II.3-4 : jamais une stacktrace brute exposee
 * au client (regle de securite : ne jamais fuiter de details d'implementation).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> gererBusiness(BusinessException ex) {
        return ResponseEntity.status(ex.getStatut()).body(ApiError.de(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> gererValidation(MethodArgumentNotValidException ex) {
        List<ApiError.ChampErreur> champs = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiError.ChampErreur(f.getField(), f.getDefaultMessage()))
                .toList();
        ApiError erreur = ApiError.deValidation("VALIDATION_ECHOUEE", "Certains champs sont invalides", champs);
        return ResponseEntity.badRequest().body(erreur);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> gererCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.de("IDENTIFIANTS_INVALIDES", "Email ou mot de passe incorrect"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> gererAcces() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.de("ACCES_REFUSE", "Vous n'avez pas les droits pour cette action"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> gererInattendue(Exception ex) {
        // Journalise cote serveur (logger) avec stacktrace, mais jamais expose au client.
        log.error("Erreur inattendue attrapee par GlobalExceptionHandler", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError.de(
            "ERREUR_INTERNE", "Une erreur inattendue est survenue. L'equipe technique a ete notifiee."));
    }
}
