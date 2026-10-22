package com.educore.modules.pedagogie.service;

import com.educore.modules.classe.dto.ClasseDto;
import com.educore.modules.classe.entity.ClasseMatiere;
import com.educore.modules.classe.repository.ClasseMatiereRepository;
import com.educore.modules.classe.service.ClasseService;
import com.educore.modules.classe.service.StructureSystemeEducatifService;
import com.educore.modules.eleve.dto.EleveDto;
import com.educore.modules.eleve.dto.InscriptionDto;
import com.educore.modules.eleve.entity.Eleve;
import com.educore.modules.eleve.repository.EleveRepository;
import com.educore.modules.eleve.service.EleveService;
import com.educore.modules.eleve.service.InscriptionService;
import com.educore.modules.enseignant.dto.EnseignantDto;
import com.educore.modules.enseignant.service.EnseignantService;
import com.educore.modules.etablissement.service.AnneeScolaireService;
import com.educore.modules.note.dto.*;
import com.educore.modules.note.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service d'orchestration du workflow pédagogique complet :
 * 1. Création d'élève
 * 2. Création de professeurs par matière
 * 3. Affectation des professeurs aux classes-matières
 * 4. Saisie des notes par les professeurs
 * 5. Calcul des moyennes
 * 6. Génération du bulletin
 */
@Service
@RequiredArgsConstructor
public class WorkflowPedagogiqueService {

    private final EleveService eleveService;
    private final EnseignantService enseignantService;
    private final InscriptionService inscriptionService;
    private final ClasseService classeService;
    private final ClasseMatiereRepository classeMatiereRepository;
    private final AnneeScolaireService anneeScolaireService;
    private final StructureSystemeEducatifService structureSystemeEducatifService;
    private final EvaluationService evaluationService;
    private final NoteService noteService;
    private final MoyenneCalculService moyenneCalculService;
    private final BulletinService bulletinService;
    private final EleveRepository eleveRepository;

    // ============================================
    // ÉTAPE 1 : Création d'un élève avec inscription
    // ============================================
    @Transactional
    public WorkflowResponse.CreationEleve creerEleveAvecInscription(
            Long etablissementId,
            WorkflowRequete.CreationEleve requete) {

        // 1. Créer le dossier élève
        EleveDto.Creation eleveCreation = new EleveDto.Creation(
                requete.nom(),
                requete.prenom(),
                requete.sexe(),
                requete.dateNaissance(),
                requete.lieuNaissance(),
                requete.nationalite(),
                requete.religion(),
                requete.groupeSanguin(),
                requete.allergies(),
                requete.matricule(),
                requete.classeId()
        );
        EleveDto.Profil eleve = eleveService.creer(etablissementId, eleveCreation);

        // Note: L'inscription doit être faite séparément via InscriptionService
        return new WorkflowResponse.CreationEleve(eleve, requete.classeId());
    }

   // ============================================
    // ÉTAPE 2 : Création d'un professeur pour une matière
    // ============================================
    @Transactional
    public EnseignantDto.CreationReponse creerProfesseurPourMatiere(
            Long etablissementId,
            WorkflowRequete.CreationProfesseur requete) {
        
        EnseignantDto.Creation enseignantCreation = new EnseignantDto.Creation(
                requete.nom(),
                requete.prenom(),
                requete.email(),
                requete.telephone(),
                requete.matricule(),
                requete.specialite(),
                requete.diplomePrincipal(),
                requete.dateEmbauche(),
                requete.typeContrat(),
                requete.salaireBase(),
                requete.tauxHoraireSupp()
        );
        
        return enseignantService.creer(etablissementId, enseignantCreation);
    }

    // ============================================
    // ÉTAPE 3 : Affectation d'un professeur à une classe-matière
    // ============================================
    @Transactional
    public void affecterProfesseurClasseMatiere(
            Long etablissementId,
            Long classeMatiereId,
            Long enseignantId) {

        ClasseMatiere classeMatiere = classeMatiereRepository.findById(classeMatiereId)
                .orElseThrow(() -> new IllegalArgumentException("ClasseMatiere non trouvée"));

        classeMatiere.setEnseignantId(enseignantId);
        classeMatiereRepository.save(classeMatiere);
    }

    // ============================================
    // ÉTAPE 4 : Saisie des notes par un professeur
    // ============================================
    @Transactional
    public WorkflowResponse.SaisieNotes saisirNotesEvaluation(
            Long etablissementId,
            Long enseignantId,
            WorkflowRequete.SaisieNotes requete) {
        
        // 1. Créer l'évaluation
        EvaluationDto.Creation evalCreation = new EvaluationDto.Creation(
                requete.classeMatiereId(),
                requete.typeEvaluationCode(),
                requete.periodeId(),
                requete.titre(),
                requete.dateEvaluation(),
                requete.bareme()
        );
        EvaluationDto.Profil evaluation = evaluationService.creer(etablissementId, enseignantId, evalCreation);
        
        // 2. Saisir les notes pour chaque élève
        NoteDto.SaisieMasse saisieMasse = new NoteDto.SaisieMasse(requete.notes());
        List<NoteDto.Profil> notes = noteService.saisirEnMasse(etablissementId, enseignantId, "PROFESSEUR", evaluation.id(), saisieMasse);

        return new WorkflowResponse.SaisieNotes(evaluation, notes);
    }

    // ============================================
    // ÉTAPE 5 : Calcul des moyennes pour une classe
    // ============================================
    @Transactional
    public List<MoyenneDto.Generale> calculerMoyennesClasse(
            Long etablissementId,
            Long classeId,
            Long periodeId) {
        
        return moyenneCalculService.recalculerPourClasse(etablissementId, classeId, periodeId);
    }

    // ============================================
    // ÉTAPE 6 : Génération des bulletins pour une classe
    // ============================================
    @Transactional
    public List<BulletinDto.Profil> genererBulletinsClasse(
            Long etablissementId,
            Long classeId,
            Long periodeId) {
        
        // 1. Calculer les moyennes
        calculerMoyennesClasse(etablissementId, classeId, periodeId);
        
        // 2. Générer les bulletins
        List<BulletinDto.Profil> bulletins = bulletinService.genererPourClasse(etablissementId, classeId, periodeId);
        
        // 3. Valider et publier chaque bulletin
        bulletins.forEach(bulletin -> {
            try {
                bulletinService.valider(etablissementId, bulletin.id());
                bulletinService.publier(etablissementId, bulletin.id());
            } catch (Exception e) {
                // Continuer même si un bulletin échoue
            }
        });
        
        return bulletins;
    }

    // ============================================
    // ÉTAPE 6 : Génération du bulletin pour un élève spécifique
    // ============================================
    @Transactional
    public BulletinDto.Profil genererBulletinEleve(
            Long etablissementId,
            Long eleveId,
            Long periodeId,
            Long classeId) {

        // Générer les bulletins pour la classe et récupérer celui de l'élève
        List<BulletinDto.Profil> bulletins = genererBulletinsClasse(etablissementId, classeId, periodeId);

        return bulletins.stream()
                .filter(b -> b.eleveId().equals(eleveId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Bulletin non généré pour cet élève"));
    }

    // ============================================
    // Workflow complet : De la création d'élève au bulletin
    // ============================================
    @Transactional
    public WorkflowResponse.Complet executerWorkflowComplet(
            Long etablissementId,
            WorkflowRequete.Complet requete) {
        
        // 1. Créer l'élève et l'inscrire
        WorkflowResponse.CreationEleve creationEleve = creerEleveAvecInscription(
                etablissementId,
                requete.eleve()
        );
        
        // 2. Affecter les professeurs aux matières de la classe
        if (requete.affectationsProfesseurs() != null) {
            requete.affectationsProfesseurs().forEach(affectation ->
                    affecterProfesseurClasseMatiere(
                            etablissementId,
                            affectation.classeMatiereId(),
                            affectation.enseignantId()
                    )
            );
        }
        
        // 3. Saisir les notes (si fournies)
        WorkflowResponse.SaisieNotes saisieNotes = null;
        if (requete.saisieNotes() != null) {
            saisieNotes = saisirNotesEvaluation(
                    etablissementId,
                    requete.saisieNotes().enseignantId(),
                    requete.saisieNotes()
            );
        }
        
        // 4. Calculer les moyennes et générer le bulletin
        List<BulletinDto.Profil> bulletins = null;
        if (requete.periodeId() != null && requete.classeId() != null) {
            try {
                bulletins = genererBulletinsClasse(etablissementId, requete.classeId(), requete.periodeId());
            } catch (Exception e) {
                // Le bulletin peut échouer si pas assez de notes
                // On continue quand même
            }
        }
        
        return new WorkflowResponse.Complet(
                creationEleve,
                saisieNotes,
                bulletins
        );
    }

    // ============================================
    // DTOs pour les requêtes de workflow
    // ============================================
    public static class WorkflowRequete {
        public record CreationEleve(
                String matricule,
                String nom,
                String prenom,
                String sexe,
                LocalDate dateNaissance,
                String lieuNaissance,
                String nationalite,
                String religion,
                String groupeSanguin,
                String allergies,
                Long classeId,
                LocalDate dateInscription
        ) {}

        public record CreationProfesseur(
                String nom,
                String prenom,
                String email,
                String telephone,
                String matricule,
                String specialite,
                String diplomePrincipal,
                LocalDate dateEmbauche,
                String typeContrat,
                BigDecimal salaireBase,
                BigDecimal tauxHoraireSupp
        ) {}

        public record AffectationProfesseur(
                Long classeMatiereId,
                Long enseignantId
        ) {}

        public record SaisieNotes(
                Long classeMatiereId,
                String typeEvaluationCode,
                Long periodeId,
                String titre,
                LocalDate dateEvaluation,
                BigDecimal bareme,
                Long enseignantId,
                List<NoteDto.Ligne> notes
        ) {}

        public record Complet(
                CreationEleve eleve,
                List<AffectationProfesseur> affectationsProfesseurs,
                SaisieNotes saisieNotes,
                Long periodeId,
                Long classeId
        ) {}
    }

    // ============================================
    // DTOs pour les réponses de workflow
    // ============================================
    public static class WorkflowResponse {
        public record CreationEleve(
                EleveDto.Profil eleve,
                Long classeId
        ) {}

        public record SaisieNotes(
                EvaluationDto.Profil evaluation,
                List<NoteDto.Profil> notes
        ) {}

        public record Complet(
                CreationEleve creationEleve,
                SaisieNotes saisieNotes,
                List<BulletinDto.Profil> bulletins
        ) {}
    }
}
