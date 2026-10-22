package com.educore.modules.personnel.service;

import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.common.util.MotDePasseUtils;
import com.educore.modules.auth.entity.Utilisateur;
import com.educore.modules.auth.repository.UtilisateurRepository;
import com.educore.modules.auth.service.UtilisateurRoleService;
import com.educore.modules.personnel.dto.PersonnelDto;
import com.educore.modules.personnel.entity.DemandeConge;
import com.educore.modules.personnel.entity.Personnel;
import com.educore.modules.personnel.repository.DemandeCongeRepository;
import com.educore.modules.personnel.repository.PersonnelRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Module 20 — Gestion du personnel (RH). */
@Service
public class PersonnelService {

    private final PersonnelRepository personnelRepository;
    private final DemandeCongeRepository congeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurRoleService utilisateurRoleService;
    private final PasswordEncoder passwordEncoder;

    public PersonnelService(PersonnelRepository personnelRepository, DemandeCongeRepository congeRepository,
                             UtilisateurRepository utilisateurRepository, UtilisateurRoleService utilisateurRoleService,
                             PasswordEncoder passwordEncoder) {
        this.personnelRepository = personnelRepository;
        this.congeRepository = congeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurRoleService = utilisateurRoleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public PersonnelDto.CreationReponse creer(Long etablissementId, PersonnelDto.Creation requete) {
        utilisateurRepository.findByEmailAndEtablissementId(requete.email(), etablissementId).ifPresent(u -> {
            throw new BusinessException("EMAIL_DEJA_UTILISE", "Un compte existe deja avec cet email.", HttpStatus.CONFLICT);
        });

        String motDePasseTemporaire = MotDePasseUtils.genererMotDePasseTemporaire();

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEtablissementId(etablissementId);
        utilisateur.setNom(requete.nom());
        utilisateur.setPrenom(requete.prenom());
        utilisateur.setEmail(requete.email());
        utilisateur.setTelephone(requete.telephone());
        utilisateur.setMatricule(requete.matricule());
        utilisateur.setMotDePasseHash(passwordEncoder.encode(motDePasseTemporaire));
        utilisateur.setDoitChangerMdp(true);
        utilisateur.setStatut(Utilisateur.StatutUtilisateur.ACTIF);
        utilisateurRepository.save(utilisateur);

        utilisateurRoleService.attribuerRoles(utilisateur.getId(), List.of(requete.roleCode()));

        Personnel personnel = new Personnel();
        personnel.setEtablissementId(etablissementId);
        personnel.setUtilisateurId(utilisateur.getId());
        personnel.setMatricule(requete.matricule());
        personnel.setFonction(requete.fonction());
        personnel.setDepartement(requete.departement());
        personnel.setDateEmbauche(requete.dateEmbauche());
        personnel.setTypeContrat(requete.typeContrat());
        personnel.setSalaireBase(requete.salaireBase());
        personnelRepository.save(personnel);

        return new PersonnelDto.CreationReponse(versProfil(personnel, utilisateur), motDePasseTemporaire);
    }

    public List<PersonnelDto.Profil> lister(Long etablissementId) {
        return personnelRepository.findByEtablissementIdOrderByMatriculeAsc(etablissementId).stream()
                .map(p -> versProfil(p, utilisateurRepository.findById(p.getUtilisateurId()).orElse(null)))
                .toList();
    }

    @Transactional
    public PersonnelDto.DemandeCongeProfil demanderConge(Long etablissementId, Long utilisateurId,
                                                          PersonnelDto.DemandeCongeCreation requete) {
        verifierAppartenance(etablissementId, utilisateurId);
        if (requete.dateFin().isBefore(requete.dateDebut())) {
            throw new BusinessException("DATES_CONGE_INVALIDES", "La date de fin doit etre posterieure a la date de debut.", HttpStatus.BAD_REQUEST);
        }
        DemandeConge conge = new DemandeConge();
        conge.setUtilisateurId(utilisateurId);
        conge.setTypeConge(requete.typeConge());
        conge.setDateDebut(requete.dateDebut());
        conge.setDateFin(requete.dateFin());
        conge.setMotif(requete.motif());
        congeRepository.save(conge);
        return versProfilConge(conge);
    }

    public List<PersonnelDto.DemandeCongeProfil> mesConges(Long etablissementId, Long utilisateurId) {
        verifierAppartenance(etablissementId, utilisateurId);
        return congeRepository.findByUtilisateurIdOrderByCreatedAtDesc(utilisateurId).stream()
                .map(this::versProfilConge).toList();
    }

    public List<PersonnelDto.DemandeCongeProfil> congesEnAttente(Long etablissementId) {
        return congeRepository.findByStatutOrderByCreatedAtDesc("en_attente").stream()
                .filter(c -> personnelRepository.findByEtablissementIdAndUtilisateurId(etablissementId, c.getUtilisateurId()).isPresent())
                .map(this::versProfilConge).toList();
    }

    @Transactional
    public PersonnelDto.DemandeCongeProfil validerConge(Long etablissementId, Long validateurId, Long congeId,
                                                         PersonnelDto.ValidationConge requete) {
        DemandeConge conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RessourceIntrouvableException("DemandeConge", congeId));
        verifierAppartenance(etablissementId, conge.getUtilisateurId());
        if (!"approuve".equals(requete.statut()) && !"refuse".equals(requete.statut())) {
            throw new BusinessException("STATUT_CONGE_INVALIDE", "Statut attendu : approuve ou refuse.", HttpStatus.BAD_REQUEST);
        }
        conge.setStatut(requete.statut());
        conge.setValideParId(validateurId);
        congeRepository.save(conge);
        return versProfilConge(conge);
    }

    private void verifierAppartenance(Long etablissementId, Long utilisateurId) {
        Utilisateur u = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur", utilisateurId));
        if (!u.getEtablissementId().equals(etablissementId)) {
            throw new RessourceIntrouvableException("Utilisateur", utilisateurId);
        }
    }

    private PersonnelDto.Profil versProfil(Personnel p, Utilisateur u) {
        return new PersonnelDto.Profil(p.getId(), p.getUtilisateurId(), p.getMatricule(),
                u != null ? u.getNom() : null, u != null ? u.getPrenom() : null,
                u != null ? u.getEmail() : null, p.getFonction(), p.getDepartement(),
                p.getDateEmbauche(), p.getTypeContrat(), p.getSalaireBase(), p.getStatut());
    }

    private PersonnelDto.DemandeCongeProfil versProfilConge(DemandeConge c) {
        return new PersonnelDto.DemandeCongeProfil(c.getId(), c.getUtilisateurId(), c.getTypeConge(),
                c.getDateDebut(), c.getDateFin(), c.getMotif(), c.getStatut(), c.getCreatedAt());
    }
}
