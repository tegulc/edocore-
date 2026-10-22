package com.educore.modules.cantine.service;

import com.educore.modules.cantine.dto.CantineDto;
import com.educore.modules.cantine.entity.AbonnementCantine;
import com.educore.modules.cantine.entity.MenuCantine;
import com.educore.modules.cantine.entity.PresenceCantine;
import com.educore.modules.cantine.repository.AbonnementCantineRepository;
import com.educore.modules.cantine.repository.MenuCantineRepository;
import com.educore.modules.cantine.repository.PresenceCantineRepository;
import com.educore.modules.eleve.service.EleveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Cantine (Module 19). La gestion des stocks (stock_cantine) est hors perimetre de cette livraison. */
@Service
public class CantineService {

    private final MenuCantineRepository menuCantineRepository;
    private final AbonnementCantineRepository abonnementCantineRepository;
    private final PresenceCantineRepository presenceCantineRepository;
    private final EleveService eleveService;

    public CantineService(MenuCantineRepository menuCantineRepository, AbonnementCantineRepository abonnementCantineRepository,
                           PresenceCantineRepository presenceCantineRepository, EleveService eleveService) {
        this.menuCantineRepository = menuCantineRepository;
        this.abonnementCantineRepository = abonnementCantineRepository;
        this.presenceCantineRepository = presenceCantineRepository;
        this.eleveService = eleveService;
    }

    @Transactional
    public CantineDto.MenuProfil planifierMenu(Long etablissementId, CantineDto.MenuCreation requete) {
        MenuCantine menu = new MenuCantine();
        menu.setEtablissementId(etablissementId);
        menu.setDateJour(requete.dateJour());
        menu.setLibellePlat(requete.libellePlat());
        menu.setInformationsNutritionnelles(requete.informationsNutritionnelles());
        menuCantineRepository.save(menu);
        return new CantineDto.MenuProfil(menu.getId(), menu.getDateJour(), menu.getLibellePlat(), menu.getInformationsNutritionnelles());
    }

    public List<CantineDto.MenuProfil> menusDeLaSemaine(Long etablissementId, LocalDate debut, LocalDate fin) {
        return menuCantineRepository.findByEtablissementIdAndDateJourBetweenOrderByDateJourAsc(etablissementId, debut, fin).stream()
                .map(m -> new CantineDto.MenuProfil(m.getId(), m.getDateJour(), m.getLibellePlat(), m.getInformationsNutritionnelles()))
                .toList();
    }

    @Transactional
    public CantineDto.AbonnementProfil abonner(Long etablissementId, Long eleveId, CantineDto.AbonnementCreation requete) {
        eleveService.trouver(etablissementId, eleveId);
        AbonnementCantine abo = abonnementCantineRepository.findByEleveIdAndPeriodeId(eleveId, requete.periodeId()).orElseGet(AbonnementCantine::new);
        abo.setEleveId(eleveId);
        abo.setPeriodeId(requete.periodeId());
        abo.setMontant(requete.montant());
        abonnementCantineRepository.save(abo);
        return new CantineDto.AbonnementProfil(abo.getId(), abo.getEleveId(), abo.getPeriodeId(), abo.getMontant(), abo.getStatutPaiement());
    }

    @Transactional
    public CantineDto.PresenceProfil pointer(Long etablissementId, CantineDto.PresenceCreation requete) {
        eleveService.trouver(etablissementId, requete.eleveId());
        PresenceCantine presence = presenceCantineRepository.findByEleveIdAndDateJour(requete.eleveId(), requete.dateJour()).orElseGet(PresenceCantine::new);
        presence.setEleveId(requete.eleveId());
        presence.setDateJour(requete.dateJour());
        presence.setStatut(requete.statut());
        presenceCantineRepository.save(presence);
        return new CantineDto.PresenceProfil(presence.getId(), presence.getEleveId(), presence.getDateJour(), presence.getStatut());
    }
}
