package com.educore.ia.bulletin.controller;

import com.educore.ia.bulletin.dto.CommentaireBulletinDto;
import com.educore.ia.bulletin.service.CommentaireBulletinService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bulletins/{bulletinId}/commentaire-ia")
@Tag(name = "Commentaires de bulletin (IA)", description = "Suggestion de commentaire, toujours soumise a validation enseignant")
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'PROFESSEUR')")
public class CommentaireBulletinController {

    private final CommentaireBulletinService commentaireBulletinService;

    public CommentaireBulletinController(CommentaireBulletinService commentaireBulletinService) {
        this.commentaireBulletinService = commentaireBulletinService;
    }

    @PostMapping("/generer")
    public ResponseEntity<CommentaireBulletinDto.Profil> generer(@PathVariable Long bulletinId) {
        return ResponseEntity.ok(commentaireBulletinService.genererPour(bulletinId));
    }

    @PutMapping("/valider")
    public ResponseEntity<CommentaireBulletinDto.Profil> valider(@PathVariable Long bulletinId, @Valid @RequestBody CommentaireBulletinDto.Validation requete) {
        return ResponseEntity.ok(commentaireBulletinService.valider(bulletinId, requete));
    }
}
