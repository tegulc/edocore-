package com.educore.ia.risque.service;

import com.educore.ia.risque.dto.AlerteRisqueDto;
import com.educore.ia.risque.entity.AlerteRisqueEchec;
import com.educore.ia.risque.entity.MoyenneGeneraleProjection;
import com.educore.ia.risque.repository.AlerteRisqueEchecRepository;
import com.educore.ia.risque.repository.IndicateursRisqueRepository;
import com.educore.ia.risque.repository.MoyenneGeneraleProjectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Detection des eleves a risque d'echec (Module 25). Score EXPLICABLE et
 * deterministe, pas un modele boite noire : conforme au principe "aucune
 * decision automatique irreversible sans validation humaine" (document
 * d'architecture, section IA) - ce score est une SUGGESTION pour le conseil
 * de classe, jamais une decision d'orientation ou de sanction automatique.
 *
 * Formule (ponderation volontairement simple et documentee, amelioree plus
 * tard avec un vrai modele si les donnees et le besoin le justifient) :
 *   score = 0.5 x (1 - moyenne/20) + 0.3 x min(absences/10, 1) + 0.2 x min(sanctions/3, 1)
 * Plage [0, 1]. Seuil "eleve a risque" fixe a 0.5 (configurable a l'usage).
 */
@Service
public class DetectionRisqueService {

    private static final MathContext PRECISION = new MathContext(3);
    private static final BigDecimal SEUIL_RISQUE = BigDecimal.valueOf(0.5);

    private final MoyenneGeneraleProjectionRepository moyenneRepository;
    private final IndicateursRisqueRepository indicateursRepository;
    private final AlerteRisqueEchecRepository alerteRepository;

    public DetectionRisqueService(MoyenneGeneraleProjectionRepository moyenneRepository, IndicateursRisqueRepository indicateursRepository,
                                   AlerteRisqueEchecRepository alerteRepository) {
        this.moyenneRepository = moyenneRepository;
        this.indicateursRepository = indicateursRepository;
        this.alerteRepository = alerteRepository;
    }

    @Transactional
    public List<AlerteRisqueDto.Profil> calculerPourClasse(Long classeId, Long periodeId, LocalDate debutPeriode, LocalDate finPeriode) {
        return moyenneRepository.findByClasseIdAndPeriodeId(classeId, periodeId).stream()
                .map(m -> calculerPourEleve(m, periodeId, debutPeriode, finPeriode))
                .toList();
    }

    private AlerteRisqueDto.Profil calculerPourEleve(MoyenneGeneraleProjection moyenne, Long periodeId, LocalDate debut, LocalDate fin) {
        BigDecimal moyenneGenerale = moyenne.getMoyenneGenerale() != null ? moyenne.getMoyenneGenerale() : BigDecimal.valueOf(10);
        long absences = indicateursRepository.compterAbsencesNonJustifiees(moyenne.getEleveId(), debut, fin);
        long sanctions = indicateursRepository.compterSanctions(moyenne.getEleveId());

        BigDecimal facteurMoyenne = BigDecimal.ONE.subtract(moyenneGenerale.divide(BigDecimal.valueOf(20), PRECISION))
                .multiply(BigDecimal.valueOf(0.5));
        BigDecimal facteurAbsences = BigDecimal.valueOf(Math.min(absences / 10.0, 1.0)).multiply(BigDecimal.valueOf(0.3));
        BigDecimal facteurDiscipline = BigDecimal.valueOf(Math.min(sanctions / 3.0, 1.0)).multiply(BigDecimal.valueOf(0.2));

        BigDecimal score = facteurMoyenne.add(facteurAbsences).add(facteurDiscipline).setScale(3, RoundingMode.HALF_UP);
        String facteursJson = """
                {"moyenne_generale": %s, "absences_non_justifiees": %d, "sanctions": %d}""".formatted(moyenneGenerale, absences, sanctions);

        AlerteRisqueEchec alerte = alerteRepository.findByEleveIdAndPeriodeId(moyenne.getEleveId(), periodeId).orElseGet(AlerteRisqueEchec::new);
        alerte.setEleveId(moyenne.getEleveId());
        alerte.setPeriodeId(periodeId);
        alerte.setScoreRisque(score);
        alerte.setFacteursIdentifies(facteursJson);
        alerteRepository.save(alerte);

        return versProfil(alerte);
    }

    public List<AlerteRisqueDto.Profil> elevesARisque(Long periodeId) {
        return alerteRepository.findByPeriodeIdAndScoreRisqueGreaterThanEqualOrderByScoreRisqueDesc(periodeId, SEUIL_RISQUE)
                .stream().map(this::versProfil).toList();
    }

    private AlerteRisqueDto.Profil versProfil(AlerteRisqueEchec a) {
        String niveau = a.getScoreRisque().compareTo(BigDecimal.valueOf(0.7)) >= 0 ? "eleve"
                : a.getScoreRisque().compareTo(SEUIL_RISQUE) >= 0 ? "modere" : "faible";
        return new AlerteRisqueDto.Profil(a.getEleveId(), a.getScoreRisque(), niveau, a.getFacteursIdentifies());
    }
}
