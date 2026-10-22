package com.educore.modules.eleve.service;

import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.classe.entity.Classe;
import com.educore.modules.classe.repository.ClasseRepository;
import com.educore.modules.eleve.dto.EleveDto;
import com.educore.modules.eleve.entity.Eleve;
import com.educore.modules.eleve.entity.Inscription;
import com.educore.modules.eleve.repository.EleveRepository;
import com.educore.modules.eleve.repository.InscriptionRepository;
import com.educore.modules.etablissement.entity.AnneeScolaire;
import com.educore.modules.etablissement.repository.AnneeScolaireRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Gestion des dossiers eleves (Module 2). Un eleve N'A PAS forcement de compte
 * Utilisateur (utilisateur_id nullable, contrairement a Parent/Enseignant) :
 * beaucoup d'etablissements africains geberont l'acces uniquement via les
 * comptes parents, surtout pour les plus jeunes niveaux.
 *
 * Regle 01 : la suppression est toujours un archivage logique (statut), jamais
 * une suppression physique (voir cahier des charges, Module 1, applique par
 * coherence a tous les dossiers sensibles).
 */
@Service
public class EleveService {

    private final EleveRepository eleveRepository;
    private final ClasseRepository classeRepository;
    private final InscriptionRepository inscriptionRepository;
    private final AnneeScolaireRepository anneeScolaireRepository;

    public EleveService(EleveRepository eleveRepository, ClasseRepository classeRepository,
                       InscriptionRepository inscriptionRepository, AnneeScolaireRepository anneeScolaireRepository) {
        this.eleveRepository = eleveRepository;
        this.classeRepository = classeRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.anneeScolaireRepository = anneeScolaireRepository;
    }

    @Transactional
    public EleveDto.Profil creer(Long etablissementId, EleveDto.Creation requete) {
        // Le matricule est maintenant obligatoire
        String matricule = requete.matricule();

        if (eleveRepository.existsByEtablissementIdAndMatricule(etablissementId, matricule)) {
            throw new BusinessException("MATRICULE_DEJA_UTILISE", "Ce matricule est deja attribue.", HttpStatus.CONFLICT);
        }

        Eleve eleve = new Eleve();
        eleve.setEtablissementId(etablissementId);
        eleve.setMatricule(matricule);
        eleve.setNom(requete.nom());
        eleve.setPrenom(requete.prenom());
        eleve.setSexe(requete.sexe());
        eleve.setDateNaissance(requete.dateNaissance());
        eleve.setLieuNaissance(requete.lieuNaissance());
        eleve.setNationalite(requete.nationalite());
        eleve.setReligion(requete.religion());
        eleve.setGroupeSanguin(requete.groupeSanguin());
        eleve.setAllergies(requete.allergies());
        eleve.setQrCode(UUID.randomUUID().toString());
        eleve.setStatut("actif");
        eleveRepository.save(eleve);

        // Inscription automatique si classeId est fourni
        if (requete.classeId() != null) {
            Classe classe = classeRepository.findById(requete.classeId())
                    .orElseThrow(() -> new RessourceIntrouvableException("CLASSE_INEXISTANTE", "Classe non trouvee"));
            
            if (!classe.getEtablissementId().equals(etablissementId)) {
                throw new BusinessException("CLASSE_HORS_ETABLISSEMENT", "Cette classe n'appartient pas a cet etablissement", HttpStatus.FORBIDDEN);
            }

            // Trouver l'annee scolaire courante
            AnneeScolaire anneeCourante = anneeScolaireRepository.findByEtablissementIdAndEstActiveTrue(etablissementId)
                    .orElseThrow(() -> new BusinessException("ANNEE_SCOLAIRE_COURANTE_NON_DEFINIE", "Aucune annee scolaire courante definie", HttpStatus.BAD_REQUEST));

            Inscription inscription = new Inscription();
            inscription.setEleveId(eleve.getId());
            inscription.setClasseId(classe.getId());
            inscription.setAnneeScolaireId(anneeCourante.getId());
            inscription.setStatut("validee");
            inscriptionRepository.save(inscription);
        }

        return versProfil(eleve);
    }

    @Transactional
    public EleveDto.Profil modifier(Long etablissementId, Long eleveId, EleveDto.Modification requete) {
        Eleve eleve = trouver(etablissementId, eleveId);
        if (requete.nom() != null) eleve.setNom(requete.nom());
        if (requete.prenom() != null) eleve.setPrenom(requete.prenom());
        if (requete.lieuNaissance() != null) eleve.setLieuNaissance(requete.lieuNaissance());
        if (requete.nationalite() != null) eleve.setNationalite(requete.nationalite());
        if (requete.religion() != null) eleve.setReligion(requete.religion());
        if (requete.groupeSanguin() != null) eleve.setGroupeSanguin(requete.groupeSanguin());
        if (requete.allergies() != null) eleve.setAllergies(requete.allergies());
        if (requete.photoUrl() != null) eleve.setPhotoUrl(requete.photoUrl());
        eleve.setUpdatedAt(Instant.now());
        return versProfil(eleve);
    }

    @Transactional
    public void changerStatut(Long etablissementId, Long eleveId, String statut) {
        Eleve eleve = trouver(etablissementId, eleveId);
        eleve.setStatut(statut);
        eleve.setUpdatedAt(Instant.now());
    }

    public Page<EleveDto.Profil> lister(Long etablissementId, Pageable pageable) {
        return eleveRepository.findByEtablissementId(etablissementId, pageable).map(this::versProfil);
    }

    /** Utilise par le Module Dashboard pour l'indicateur "eleves inscrits". */
    public long compterActifs(Long etablissementId) {
        return eleveRepository.countByEtablissementIdAndStatut(etablissementId, "actif");
    }

    public EleveDto.Profil obtenir(Long etablissementId, Long eleveId) {
        return versProfil(trouver(etablissementId, eleveId));
    }

    public Eleve trouver(Long etablissementId, Long eleveId) {
        Eleve eleve = eleveRepository.findById(eleveId)
                .orElseThrow(() -> new RessourceIntrouvableException("Eleve", eleveId));
        if (!eleve.getEtablissementId().equals(etablissementId)) {
            throw new RessourceIntrouvableException("Eleve", eleveId);
        }
        return eleve;
    }

    public EleveDto.Profil obtenirMonProfil(Long etablissementId, Long utilisateurId) {
        Eleve eleve = eleveRepository.findByEtablissementIdAndUtilisateurId(etablissementId, utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Eleve", utilisateurId));
        return versProfil(eleve);
    }

    public Long obtenirIdParUtilisateurId(Long etablissementId, Long utilisateurId) {
        return eleveRepository.findByEtablissementIdAndUtilisateurId(etablissementId, utilisateurId)
                .map(Eleve::getId)
                .orElseThrow(() -> new RessourceIntrouvableException("Eleve", utilisateurId));
    }

    /** Format : ELV-{annee}-{sequence}. Simple et lisible ; un vrai compteur atomique viendra si le volume l'exige. */
    private String genererMatricule(Long etablissementId) {
        long rang = eleveRepository.count() + 1;
        return "ELV-%d-%04d".formatted(LocalDate.now().getYear(), rang);
    }

    private EleveDto.Profil versProfil(Eleve e) {
        return new EleveDto.Profil(e.getId(), e.getUuid().toString(), e.getMatricule(), e.getNom(), e.getPrenom(),
                e.getSexe(), e.getDateNaissance(), e.getLieuNaissance(), e.getNationalite(), e.getGroupeSanguin(),
                e.getAllergies(), e.getPhotoUrl(), e.getQrCode(), e.getStatut(), e.getDateInscription());
    }
}
