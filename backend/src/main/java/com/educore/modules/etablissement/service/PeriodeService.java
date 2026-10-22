package com.educore.modules.etablissement.service;

import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.etablissement.dto.PeriodeDto;
import com.educore.modules.etablissement.entity.AnneeScolaire;
import com.educore.modules.etablissement.entity.Periode;
import com.educore.modules.etablissement.repository.PeriodeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Periodes pedagogiques (trimestres/sequences/semestres) d'une annee scolaire.
 * Regle 01 : une periode reste toujours contenue dans les dates de son annee.
 * Regle 02 : les periodes d'une meme annee ne se chevauchent jamais.
 * Regle 03 : une periode cloturee (bulletins publies) ne peut plus etre modifiee.
 */
@Service
public class PeriodeService {

    private final PeriodeRepository periodeRepository;
    private final AnneeScolaireService anneeScolaireService;

    public PeriodeService(PeriodeRepository periodeRepository, AnneeScolaireService anneeScolaireService) {
        this.periodeRepository = periodeRepository;
        this.anneeScolaireService = anneeScolaireService;
    }

    @Transactional
    public PeriodeDto.Profil creer(Long etablissementId, Long anneeId, PeriodeDto.Creation requete) {
        AnneeScolaire annee = anneeScolaireService.trouverDansEtablissement(etablissementId, anneeId);

        if (!requete.dateFin().isAfter(requete.dateDebut())) {
            throw new BusinessException("PERIODE_INVALIDE", "La date de fin doit etre posterieure a la date de debut.", HttpStatus.BAD_REQUEST);
        }
        if (requete.dateDebut().isBefore(annee.getDateDebut()) || requete.dateFin().isAfter(annee.getDateFin())) {
            throw new BusinessException("PERIODE_HORS_ANNEE",
                    "Les dates de la periode doivent rester comprises dans celles de l'annee scolaire (%s - %s).".formatted(annee.getDateDebut(), annee.getDateFin()),
                    HttpStatus.BAD_REQUEST);
        }
        if (periodeRepository.existsByAnneeScolaireIdAndTypePeriodeAndNumero(anneeId, requete.typePeriode(), requete.numero())) {
            throw new BusinessException("PERIODE_DEJA_EXISTANTE", "Cette periode existe deja pour cette annee.", HttpStatus.CONFLICT);
        }

        boolean chevauche = periodeRepository.findByAnneeScolaireIdOrderByNumeroAsc(anneeId).stream()
                .anyMatch(p -> requete.dateDebut().isBefore(p.getDateFin()) && requete.dateFin().isAfter(p.getDateDebut()));
        if (chevauche) {
            throw new BusinessException("PERIODES_CHEVAUCHENT", "Cette periode chevauche une periode existante de la meme annee.", HttpStatus.CONFLICT);
        }

        Periode periode = new Periode();
        periode.setAnneeScolaireId(anneeId);
        periode.setTypePeriode(requete.typePeriode());
        periode.setNumero(requete.numero());
        periode.setLibelle(requete.libelle());
        periode.setDateDebut(requete.dateDebut());
        periode.setDateFin(requete.dateFin());
        periodeRepository.save(periode);

        return versProfil(periode);
    }

    @Transactional
    public PeriodeDto.Profil cloturer(Long etablissementId, Long anneeId, Long periodeId) {
        anneeScolaireService.trouverDansEtablissement(etablissementId, anneeId); // verifie l'appartenance au tenant
        Periode periode = trouverDansAnnee(anneeId, periodeId);
        if (periode.isEstCloturee()) {
            throw new BusinessException("PERIODE_DEJA_CLOTUREE", "Cette periode est deja cloturee.", HttpStatus.CONFLICT);
        }
        periode.setEstCloturee(true);
        return versProfil(periode);
    }

    public List<PeriodeDto.Profil> lister(Long etablissementId, Long anneeId) {
        anneeScolaireService.trouverDansEtablissement(etablissementId, anneeId);
        return periodeRepository.findByAnneeScolaireIdOrderByNumeroAsc(anneeId).stream().map(this::versProfil).toList();
    }

    /** Recherche directe par id (sans connaitre l'annee a l'avance) - utilise par le module Notes/Bulletins. */
    public PeriodeDto.Profil obtenir(Long etablissementId, Long periodeId) {
        Periode periode = periodeRepository.findById(periodeId)
                .orElseThrow(() -> new RessourceIntrouvableException("Periode", periodeId));
        anneeScolaireService.trouverDansEtablissement(etablissementId, periode.getAnneeScolaireId()); // verifie le tenant
        return versProfil(periode);
    }

    private Periode trouverDansAnnee(Long anneeId, Long periodeId) {
        Periode periode = periodeRepository.findById(periodeId)
                .orElseThrow(() -> new RessourceIntrouvableException("Periode", periodeId));
        if (!periode.getAnneeScolaireId().equals(anneeId)) {
            throw new RessourceIntrouvableException("Periode", periodeId);
        }
        return periode;
    }

    private PeriodeDto.Profil versProfil(Periode p) {
        return new PeriodeDto.Profil(p.getId(), p.getAnneeScolaireId(), p.getTypePeriode(), p.getNumero(),
                p.getLibelle(), p.getDateDebut(), p.getDateFin(), p.isEstCloturee());
    }
}
