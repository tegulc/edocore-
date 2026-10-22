package com.educore.modules.classe.controller;

import com.educore.modules.classe.dto.CycleDto;
import com.educore.modules.classe.dto.EtablissementLangueOptionDto;
import com.educore.modules.classe.dto.MatiereDto;
import com.educore.modules.classe.dto.MatiereNiveauSerieDto;
import com.educore.modules.classe.dto.SerieDto;
import com.educore.modules.classe.dto.SousSystemeDto;
import com.educore.modules.classe.service.StructureSystemeEducatifService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/structure-systeme-educatif")
@RequiredArgsConstructor
public class StructureSystemeEducatifController {

    private final StructureSystemeEducatifService structureSystemeEducatifService;

    // Référentiels globaux (accessibles à tous les utilisateurs connectés)
    @GetMapping("/sous-systemes")
    public ResponseEntity<List<SousSystemeDto>> listerSousSystemes() {
        return ResponseEntity.ok(structureSystemeEducatifService.listerSousSystemes());
    }

    @GetMapping("/cycles")
    public ResponseEntity<List<CycleDto>> listerCycles() {
        return ResponseEntity.ok(structureSystemeEducatifService.listerCycles());
    }

    @GetMapping("/series")
    public ResponseEntity<List<SerieDto.Profil>> listerSeries(@RequestParam(required = false) String sousSystemeCode) {
        if (sousSystemeCode != null) {
            return ResponseEntity.ok(structureSystemeEducatifService.listerSeriesParSousSysteme(sousSystemeCode));
        }
        return ResponseEntity.ok(structureSystemeEducatifService.listerSeriesParSousSysteme("FRANCOPHONE"));
    }

    @GetMapping("/matieres")
    public ResponseEntity<List<MatiereDto.Profil>> listerMatieres(@RequestParam(required = false) String typeMatiere) {
        return ResponseEntity.ok(structureSystemeEducatifService.listerMatieresParType(typeMatiere));
    }

    // Initialisation des niveaux par défaut (réservé à l'admin)
    @PostMapping("/niveaux/initialiser")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<Void> initialiserNiveauxParDefaut(
            @RequestParam Long etablissementId,
            @RequestParam(defaultValue = "FRANCOPHONE") String sousSystemeCode) {
        structureSystemeEducatifService.initialiserNiveauxParDefaut(etablissementId, sousSystemeCode);
        return ResponseEntity.ok().build();
    }

    // Configuration des matières par niveau et série
    @PostMapping("/matieres-niveau-serie")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR', 'CENSEUR')")
    public ResponseEntity<MatiereNiveauSerieDto> configurerMatiereNiveauSerie(
            @RequestBody MatiereNiveauSerieDto dto) {
        return ResponseEntity.ok(structureSystemeEducatifService.configurerMatiereNiveauSerie(dto));
    }

    @GetMapping("/matieres-niveau-serie")
    public ResponseEntity<List<MatiereNiveauSerieDto>> listerMatieresParNiveau(
            @RequestParam Long etablissementId,
            @RequestParam Long niveauId,
            @RequestParam(required = false) Long serieId) {
        if (serieId != null) {
            return ResponseEntity.ok(structureSystemeEducatifService.listerMatieresParNiveauEtSerie(etablissementId, niveauId, serieId));
        }
        return ResponseEntity.ok(structureSystemeEducatifService.listerMatieresParNiveau(etablissementId, niveauId));
    }

    // Configuration des langues/options par établissement
    @PostMapping("/langues-options")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
    public ResponseEntity<EtablissementLangueOptionDto> ajouterLangueOption(
            @RequestBody EtablissementLangueOptionDto dto) {
        return ResponseEntity.ok(structureSystemeEducatifService.ajouterLangueOption(dto));
    }

    @GetMapping("/langues-options")
    public ResponseEntity<List<EtablissementLangueOptionDto>> listerLanguesOptions(
            @RequestParam Long etablissementId) {
        return ResponseEntity.ok(structureSystemeEducatifService.listerLanguesOptions(etablissementId));
    }
}
