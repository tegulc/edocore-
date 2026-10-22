package com.educore.modules.pedagogie.controller;

import com.educore.modules.note.dto.BulletinDto;
import com.educore.modules.note.dto.MoyenneDto;
import com.educore.modules.pedagogie.service.WorkflowPedagogiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workflow-pedagogique")
@RequiredArgsConstructor
public class WorkflowPedagogiqueController {

    private final WorkflowPedagogiqueService workflowPedagogiqueService;

    // ============================================
    // ÉTAPE 1 : Création d'élève avec inscription
    // ============================================
    @PostMapping("/eleves")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    public ResponseEntity<WorkflowPedagogiqueService.WorkflowResponse.CreationEleve> creerEleveAvecInscription(
            @RequestParam Long etablissementId,
            @RequestBody WorkflowPedagogiqueService.WorkflowRequete.CreationEleve requete) {
        return ResponseEntity.ok(workflowPedagogiqueService.creerEleveAvecInscription(etablissementId, requete));
    }

    // ============================================
    // ÉTAPE 2 : Création de professeur pour une matière
    // ============================================
    @PostMapping("/professeurs")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<com.educore.modules.enseignant.dto.EnseignantDto.CreationReponse> creerProfesseurPourMatiere(
            @RequestParam Long etablissementId,
            @RequestBody WorkflowPedagogiqueService.WorkflowRequete.CreationProfesseur requete) {
        return ResponseEntity.ok(workflowPedagogiqueService.creerProfesseurPourMatiere(etablissementId, requete));
    }

    // ============================================
    // ÉTAPE 3 : Affectation professeur à classe-matière
    // ============================================
    @PostMapping("/affectations-professeur")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    public ResponseEntity<Void> affecterProfesseurClasseMatiere(
            @RequestParam Long etablissementId,
            @RequestParam Long classeMatiereId,
            @RequestParam Long enseignantId) {
        workflowPedagogiqueService.affecterProfesseurClasseMatiere(etablissementId, classeMatiereId, enseignantId);
        return ResponseEntity.ok().build();
    }

    // ============================================
    // ÉTAPE 4 : Saisie des notes par professeur
    // ============================================
    @PostMapping("/evaluations/notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'PROFESSEUR')")
    public ResponseEntity<WorkflowPedagogiqueService.WorkflowResponse.SaisieNotes> saisirNotesEvaluation(
            @RequestParam Long etablissementId,
            @RequestParam Long enseignantId,
            @RequestBody WorkflowPedagogiqueService.WorkflowRequete.SaisieNotes requete) {
        return ResponseEntity.ok(workflowPedagogiqueService.saisirNotesEvaluation(etablissementId, enseignantId, requete));
    }

    // ============================================
    // ÉTAPE 5 : Calcul des moyennes pour une classe
    // ============================================
    @PostMapping("/classes/{classeId}/moyennes")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR', 'PROFESSEUR')")
    public ResponseEntity<List<MoyenneDto.Generale>> calculerMoyennesClasse(
            @RequestParam Long etablissementId,
            @PathVariable Long classeId,
            @RequestParam Long periodeId) {
        return ResponseEntity.ok(workflowPedagogiqueService.calculerMoyennesClasse(etablissementId, classeId, periodeId));
    }

    // ============================================
    // ÉTAPE 6 : Génération des bulletins pour une classe
    // ============================================
    @PostMapping("/classes/{classeId}/bulletins")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    public ResponseEntity<List<BulletinDto.Profil>> genererBulletinsClasse(
            @RequestParam Long etablissementId,
            @PathVariable Long classeId,
            @RequestParam Long periodeId) {
        return ResponseEntity.ok(workflowPedagogiqueService.genererBulletinsClasse(etablissementId, classeId, periodeId));
    }

    // ============================================
    // ÉTAPE 6 : Génération du bulletin pour un élève spécifique
    // ============================================
    @PostMapping("/eleves/{eleveId}/bulletin")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    public ResponseEntity<BulletinDto.Profil> genererBulletinEleve(
            @RequestParam Long etablissementId,
            @PathVariable Long eleveId,
            @RequestParam Long periodeId,
            @RequestParam Long classeId) {
        return ResponseEntity.ok(workflowPedagogiqueService.genererBulletinEleve(etablissementId, eleveId, periodeId, classeId));
    }

    // ============================================
    // Workflow complet
    // ============================================
    @PostMapping("/complet")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<WorkflowPedagogiqueService.WorkflowResponse.Complet> executerWorkflowComplet(
            @RequestParam Long etablissementId,
            @RequestBody WorkflowPedagogiqueService.WorkflowRequete.Complet requete) {
        return ResponseEntity.ok(workflowPedagogiqueService.executerWorkflowComplet(etablissementId, requete));
    }
}
