package com.educore.modules.classe.controller;

import com.educore.modules.classe.dto.ClasseDto;
import com.educore.modules.classe.dto.ClasseMatiereDto;
import com.educore.modules.classe.service.ClasseMatiereService;
import com.educore.modules.classe.service.ClasseService;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Classes de l'annee scolaire active + affectation des matieres/enseignants. */
@RestController
@RequestMapping("/api/v1/classes")
@Tag(name = "Classes", description = "Classes de l'annee active et affectation des matieres")
public class ClasseController {

    private final ClasseService classeService;
    private final ClasseMatiereService classeMatiereService;

    public ClasseController(ClasseService classeService, ClasseMatiereService classeMatiereService) {
        this.classeService = classeService;
        this.classeMatiereService = classeMatiereService;
    }

    @GetMapping
    @Operation(summary = "Liste les classes de l'annee scolaire active")
    public ResponseEntity<List<ClasseDto.Profil>> lister(@AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(classeService.lister(principal.etablissementId()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    @Operation(summary = "Cree une classe pour l'annee scolaire active")
    public ResponseEntity<ClasseDto.Profil> creer(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                    @Valid @RequestBody ClasseDto.Creation requete) {
        return ResponseEntity.ok(classeService.creer(principal.etablissementId(), requete));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    public ResponseEntity<ClasseDto.Profil> modifier(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                       @PathVariable Long id, @Valid @RequestBody ClasseDto.Modification requete) {
        return ResponseEntity.ok(classeService.modifier(principal.etablissementId(), id, requete));
    }

    @GetMapping("/{id}/matieres")
    @Operation(summary = "Liste les matieres affectees a cette classe (coefficient, volume horaire, enseignant)")
    public ResponseEntity<List<ClasseMatiereDto.Profil>> matieres(@AuthenticationPrincipal UtilisateurPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(classeMatiereService.lister(principal.etablissementId(), id));
    }

    @PostMapping("/{id}/matieres")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    @Operation(summary = "Affecte une matiere (et un enseignant) a cette classe")
    public ResponseEntity<ClasseMatiereDto.Profil> affecterMatiere(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                     @PathVariable Long id, @Valid @RequestBody ClasseMatiereDto.Affectation requete) {
        return ResponseEntity.ok(classeMatiereService.affecter(principal.etablissementId(), id, requete));
    }
}
