package com.educore.modules.presence.service;

import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.eleve.service.EleveService;
import com.educore.modules.presence.dto.PresenceDto;
import com.educore.modules.presence.entity.PresenceEleve;
import com.educore.modules.presence.repository.PresenceEleveRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Presence des eleves (Module 12). Regle 01 : un seul enregistrement par
 * (eleve, cours, jour) - evite qu'un second pointage n'ecrase silencieusement
 * le premier sans le vouloir ; on met a jour l'existant au lieu d'en creer un
 * second si l'appelant repointe le meme (eleve, classe_matiere, jour).
 */
@Service
public class PresenceService {

    private final PresenceEleveRepository presenceEleveRepository;
    private final EleveService eleveService;

    public PresenceService(PresenceEleveRepository presenceEleveRepository, EleveService eleveService) {
        this.presenceEleveRepository = presenceEleveRepository;
        this.eleveService = eleveService;
    }

    @Transactional
    public PresenceDto.Profil enregistrer(Long etablissementId, Long enregistreParId, PresenceDto.Enregistrement requete) {
        eleveService.obtenir(etablissementId, requete.eleveId()); // verifie l'appartenance au tenant

        PresenceEleve presence = requete.classeMatiereId() != null
                ? presenceEleveRepository.findByEleveIdAndClasseMatiereIdAndDateJour(requete.eleveId(), requete.classeMatiereId(), requete.dateJour())
                        .orElseGet(PresenceEleve::new)
                : new PresenceEleve();

        presence.setEleveId(requete.eleveId());
        presence.setClasseMatiereId(requete.classeMatiereId());
        presence.setDateJour(requete.dateJour());
        presence.setTypePresence(requete.typePresence());
        presence.setMethodePointage(requete.methodePointage() != null ? requete.methodePointage() : "manuel");
        presence.setEnregistreParId(enregistreParId);
        presenceEleveRepository.save(presence);

        return versProfil(presence);
    }

    @Transactional
    public PresenceDto.Profil justifier(Long etablissementId, Long presenceId, PresenceDto.Justification requete) {
        PresenceEleve presence = presenceEleveRepository.findById(presenceId)
                .orElseThrow(() -> new RessourceIntrouvableException("Presence", presenceId));
        eleveService.obtenir(etablissementId, presence.getEleveId()); // verifie l'appartenance au tenant

        if ("present".equals(presence.getTypePresence())) {
            throw new BusinessException("PRESENCE_NON_JUSTIFIABLE", "Un pointage 'present' n'a pas besoin de justification.", HttpStatus.BAD_REQUEST);
        }
        presence.setJustifie(true);
        presence.setMotifJustification(requete.motif());
        return versProfil(presence);
    }

    public List<PresenceDto.Profil> historique(Long etablissementId, Long eleveId, LocalDate debut, LocalDate fin) {
        eleveService.obtenir(etablissementId, eleveId);
        return presenceEleveRepository.findByEleveIdAndDateJourBetweenOrderByDateJourDesc(eleveId, debut, fin)
                .stream().map(this::versProfil).toList();
    }

    public PresenceDto.Bilan bilan(Long etablissementId, Long eleveId, LocalDate debut, LocalDate fin) {
        eleveService.obtenir(etablissementId, eleveId);
        long absences = presenceEleveRepository.countByEleveIdAndTypePresenceAndJustifieFalseAndDateJourBetween(eleveId, "absent", debut, fin);
        long retards = presenceEleveRepository.countByEleveIdAndTypePresenceAndJustifieFalseAndDateJourBetween(eleveId, "retard", debut, fin);
        return new PresenceDto.Bilan(absences, retards);
    }

    /** Utilise par le Module Dashboard pour l'indicateur "taux de presence du jour". Retourne -1 si aucun pointage n'existe encore aujourd'hui. */
    public double tauxPresenceDuJour(Long etablissementId, LocalDate jour) {
        long total = presenceEleveRepository.compterPresencesDuJour(etablissementId, jour);
        if (total == 0) return -1;
        long presents = presenceEleveRepository.compterPresentsDuJour(etablissementId, jour);
        return Math.round((presents * 1000.0) / total) / 10.0; // pourcentage a une decimale
    }

    private PresenceDto.Profil versProfil(PresenceEleve p) {
        return new PresenceDto.Profil(p.getId(), p.getEleveId(), p.getClasseMatiereId(), p.getDateJour(),
                p.getTypePresence(), p.isJustifie(), p.getMotifJustification());
    }
}
