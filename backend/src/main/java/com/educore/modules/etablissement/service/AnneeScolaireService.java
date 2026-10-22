package com.educore.modules.etablissement.service;

import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.etablissement.dto.AnneeScolaireDto;
import com.educore.modules.etablissement.entity.AnneeScolaire;
import com.educore.modules.etablissement.repository.AnneeScolaireRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

/**
 * Regle metier centrale (Module socle multi-tenant) : UNE SEULE annee scolaire
 * peut etre active a la fois par etablissement. Toute la plateforme (notes,
 * presences, paiements, emploi du temps...) filtrera par defaut sur cette
 * annee active plutot que d'exiger un choix explicite a chaque ecran.
 */
@Service
public class AnneeScolaireService {

    private final AnneeScolaireRepository anneeScolaireRepository;

    public AnneeScolaireService(AnneeScolaireRepository anneeScolaireRepository) {
        this.anneeScolaireRepository = anneeScolaireRepository;
    }

    @Transactional
    public AnneeScolaireDto.Profil creer(Long etablissementId, AnneeScolaireDto.Creation requete) {
        if (!requete.dateFin().isAfter(requete.dateDebut())) {
            throw new BusinessException("PERIODE_INVALIDE", "La date de fin doit etre posterieure a la date de debut.", HttpStatus.BAD_REQUEST);
        }
        if (anneeScolaireRepository.existsByEtablissementIdAndLibelle(etablissementId, requete.libelle())) {
            throw new BusinessException("ANNEE_DEJA_EXISTANTE", "Une annee scolaire porte deja ce libelle.", HttpStatus.CONFLICT);
        }

        AnneeScolaire annee = new AnneeScolaire();
        annee.setEtablissementId(etablissementId);
        annee.setLibelle(requete.libelle());
        annee.setDateDebut(requete.dateDebut());
        annee.setDateFin(requete.dateFin());
        // La toute premiere annee d'un etablissement est activee automatiquement (sinon la
        // plateforme serait inutilisable tant que l'admin n'a pas pense a l'activer manuellement).
        annee.setEstActive(anneeScolaireRepository.findByEtablissementIdAndEstActiveTrue(etablissementId).isEmpty());
        anneeScolaireRepository.save(annee);

        return versProfil(annee);
    }

    /** Utilise par EtablissementService.inscrire() pour que la plateforme soit utilisable des l'inscription. */
    @Transactional
    public AnneeScolaireDto.Profil creerAnneeParDefaut(Long etablissementId) {
        int anneeCourante = LocalDate.now().getYear();
        // Convention Afrique francophone : annee scolaire a cheval sur deux annees civiles (sept -> juillet).
        boolean apresRentree = LocalDate.now().getMonthValue() >= Month.AUGUST.getValue();
        int debut = apresRentree ? anneeCourante : anneeCourante - 1;

        return creer(etablissementId, new AnneeScolaireDto.Creation(
                debut + "-" + (debut + 1),
                LocalDate.of(debut, Month.SEPTEMBER, 1),
                LocalDate.of(debut + 1, Month.JULY, 31)
        ));
    }

    /** Active cette annee et desactive automatiquement toute autre annee active du meme etablissement. */
    @Transactional
    public AnneeScolaireDto.Profil activer(Long etablissementId, Long anneeId) {
        AnneeScolaire annee = trouverDansEtablissement(etablissementId, anneeId);
        anneeScolaireRepository.desactiverToutesLesAnnees(etablissementId);
        annee.setEstActive(true);
        return versProfil(annee);
    }

    public List<AnneeScolaireDto.Profil> lister(Long etablissementId) {
        return anneeScolaireRepository.findByEtablissementIdOrderByDateDebutDesc(etablissementId)
                .stream().map(this::versProfil).toList();
    }

    public AnneeScolaireDto.Profil obtenirActive(Long etablissementId) {
        return anneeScolaireRepository.findByEtablissementIdAndEstActiveTrue(etablissementId)
                .map(this::versProfil)
                .orElseThrow(() -> new BusinessException("AUCUNE_ANNEE_ACTIVE",
                        "Aucune annee scolaire active. Contactez l'administrateur de l'etablissement.", HttpStatus.CONFLICT));
    }

    /** Utilise par PeriodeService pour valider qu'une periode reste dans les bornes de son annee. */
    AnneeScolaire trouverDansEtablissement(Long etablissementId, Long anneeId) {
        AnneeScolaire annee = anneeScolaireRepository.findById(anneeId)
                .orElseThrow(() -> new RessourceIntrouvableException("AnneeScolaire", anneeId));
        if (!annee.getEtablissementId().equals(etablissementId)) {
            throw new RessourceIntrouvableException("AnneeScolaire", anneeId);
        }
        return annee;
    }

    private AnneeScolaireDto.Profil versProfil(AnneeScolaire a) {
        return new AnneeScolaireDto.Profil(a.getId(), a.getLibelle(), a.getDateDebut(), a.getDateFin(), a.isEstActive());
    }
}
