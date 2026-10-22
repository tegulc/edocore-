package com.educore.modules.note.controller;

import com.educore.modules.note.dto.EvaluationDto;
import com.educore.modules.note.dto.NoteDto;
import com.educore.modules.note.service.EvaluationService;
import com.educore.modules.note.service.NoteService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/evaluations")
@Tag(name = "Evaluations & Notes", description = "Devoirs/compositions, saisie et validation des notes")
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final NoteService noteService;

    public EvaluationController(EvaluationService evaluationService, NoteService noteService) {
        this.evaluationService = evaluationService;
        this.noteService = noteService;
    }

    @GetMapping
    @Operation(summary = "Liste les evaluations d'une matiere de classe pour une periode")
    public ResponseEntity<List<EvaluationDto.Profil>> lister(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                @RequestParam Long classeMatiereId, @RequestParam Long periodeId) {
        return ResponseEntity.ok(evaluationService.listerParClasseMatiereEtPeriode(principal.etablissementId(), classeMatiereId, periodeId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'PROFESSEUR')")
    @Operation(summary = "Cree une evaluation (devoir, composition, examen...)")
    public ResponseEntity<EvaluationDto.Profil> creer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                        @Valid @RequestBody EvaluationDto.Creation requete) {
        return ResponseEntity.ok(evaluationService.creer(principal.etablissementId(), principal.utilisateurId(), requete));
    }

    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    @Operation(summary = "Valide l'evaluation : verrouille les notes, prealable au calcul des moyennes")
    public ResponseEntity<Void> valider(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long id) {
        evaluationService.valider(principal.etablissementId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/notes")
    @Operation(summary = "Liste les notes saisies pour cette evaluation")
    public ResponseEntity<List<NoteDto.Profil>> notes(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(noteService.lister(principal.etablissementId(), id));
    }

    @PostMapping("/{id}/notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'PROFESSEUR')")
    @Operation(summary = "Saisit ou met a jour les notes en masse pour cette evaluation")
    public ResponseEntity<List<NoteDto.Profil>> saisir(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                          @PathVariable Long id, @Valid @RequestBody NoteDto.SaisieMasse requete) {
        return ResponseEntity.ok(noteService.saisirEnMasse(principal.etablissementId(), principal.utilisateurId(),
                principal.rolePrincipal(), id, requete));
    }
}
