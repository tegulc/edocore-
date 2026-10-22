package com.educore.common.audit;

import com.educore.common.dto.PageDto;
import com.educore.common.dto.PaginationUtils;
import com.educore.security.UtilisateurPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/journal-audit")
@Tag(name = "Journal d'audit", description = "Consultation des actions sensibles (Module 28)")
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR')")
public class JournalAuditController {

    private final JournalAuditRepository journalAuditRepository;

    public JournalAuditController(JournalAuditRepository journalAuditRepository) {
        this.journalAuditRepository = journalAuditRepository;
    }

    @GetMapping
    @Operation(summary = "Journal d'audit pagine de l'etablissement")
    public ResponseEntity<PageDto<JournalAuditDto.Profil>> lister(@AuthenticationPrincipal UtilisateurPrincipal principal,
                                                                   @RequestParam(required = false) Integer page,
                                                                   @RequestParam(required = false) Integer taille,
                                                                   @RequestParam(required = false) String tableCible) {
        var pageable = PaginationUtils.construire(page, taille);
        var resultat = tableCible != null && !tableCible.isBlank()
                ? journalAuditRepository.findByEtablissementIdAndTableCibleOrderByHorodatageDesc(
                        principal.etablissementId(), tableCible, pageable)
                : journalAuditRepository.findByEtablissementIdOrderByHorodatageDesc(
                        principal.etablissementId(), pageable);
        return ResponseEntity.ok(PageDto.depuis(resultat, JournalAuditController::versProfil));
    }

    private static JournalAuditDto.Profil versProfil(JournalAudit entree) {
        return new JournalAuditDto.Profil(entree.getId(), entree.getUtilisateurId(), entree.getAction(),
                entree.getTableCible(), entree.getEnregistrementId(), entree.getAdresseIp(), entree.getHorodatage());
    }
}
