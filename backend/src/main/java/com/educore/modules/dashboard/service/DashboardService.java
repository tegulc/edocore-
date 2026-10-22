package com.educore.modules.dashboard.service;

import com.educore.modules.dashboard.dto.DashboardDto;
import com.educore.modules.eleve.service.EleveService;
import com.educore.modules.enseignant.service.EnseignantService;
import com.educore.modules.paiement.service.PaiementService;
import com.educore.modules.presence.service.PresenceService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Tableau de bord consolide (Module 26). Agrege des indicateurs deja calcules
 * par d'autres modules, jamais de logique metier propre - un Dashboard ne
 * doit rien calculer qu'un autre module ne sache deja produire (principe de
 * responsabilite unique applique aux modules eux-memes).
 */
@Service
public class DashboardService {

    private final EleveService eleveService;
    private final PresenceService presenceService;
    private final PaiementService paiementService;
    private final EnseignantService enseignantService;

    public DashboardService(EleveService eleveService, PresenceService presenceService,
                             PaiementService paiementService, EnseignantService enseignantService) {
        this.eleveService = eleveService;
        this.presenceService = presenceService;
        this.paiementService = paiementService;
        this.enseignantService = enseignantService;
    }

    public DashboardDto.Indicateurs indicateurs(Long etablissementId) {
        long elevesInscrits = eleveService.compterActifs(etablissementId);
        double tauxPresence = presenceService.tauxPresenceDuJour(etablissementId, LocalDate.now());
        var paiementsDuMois = paiementService.sommeConfirmeeCeMois(etablissementId);
        long personnelActif = enseignantService.lister(etablissementId).size();

        return new DashboardDto.Indicateurs(elevesInscrits, tauxPresence >= 0 ? tauxPresence : null, paiementsDuMois, personnelActif);
    }
}
