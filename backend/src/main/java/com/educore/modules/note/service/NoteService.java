package com.educore.modules.note.service;

import com.educore.common.exception.BusinessException;
import com.educore.modules.classe.service.ClasseMatiereService;
import com.educore.modules.eleve.service.EleveService;
import com.educore.modules.enseignant.service.EnseignantService;
import com.educore.modules.note.dto.NoteDto;
import com.educore.modules.note.entity.Evaluation;
import com.educore.modules.note.entity.Note;
import com.educore.modules.note.repository.NoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Saisie des notes (Module 8). Regle 01 : impossible de saisir/modifier une
 * note sur une evaluation VALIDEE ou PUBLIEE (verrouillage, evite qu'un
 * bulletin deja calcule se retrouve incoherent avec les notes sources).
 * Regle 02 : un PROFESSEUR ne peut saisir que sur SES PROPRES classe_matiere
 * (verifie via l'enseignant_id de l'affectation), jamais celles d'un collegue -
 * les roles administratifs (ADMIN/DIRECTEUR/CENSEUR) ne sont pas soumis a
 * cette restriction (delegation/correction possible).
 * Regle 03 : la note doit rester dans l'intervalle [0, bareme] sauf si l'eleve est marque absent.
 */
@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final EvaluationService evaluationService;
    private final ClasseMatiereService classeMatiereService;
    private final EnseignantService enseignantService;
    private final EleveService eleveService;

    public NoteService(NoteRepository noteRepository, EvaluationService evaluationService, ClasseMatiereService classeMatiereService,
                        EnseignantService enseignantService, EleveService eleveService) {
        this.noteRepository = noteRepository;
        this.evaluationService = evaluationService;
        this.classeMatiereService = classeMatiereService;
        this.enseignantService = enseignantService;
        this.eleveService = eleveService;
    }

    @Transactional
    public List<NoteDto.Profil> saisirEnMasse(Long etablissementId, Long principalId, String rolePrincipal,
                                               Long evaluationId, NoteDto.SaisieMasse requete) {
        Evaluation evaluation = evaluationService.trouver(etablissementId, evaluationId);
        if ("validee".equals(evaluation.getStatut()) || "publiee".equals(evaluation.getStatut())) {
            throw new BusinessException("EVALUATION_VERROUILLEE", "Cette evaluation est validee : les notes ne sont plus modifiables.", HttpStatus.CONFLICT);
        }

        if ("PROFESSEUR".equals(rolePrincipal)) {
            var classeMatiere = classeMatiereService.obtenir(etablissementId, evaluation.getClasseMatiereId());
            Long enseignantIdConnecte = enseignantService.obtenirIdParUtilisateurId(principalId);
            if (enseignantIdConnecte == null || !enseignantIdConnecte.equals(classeMatiere.enseignantId())) {
                throw new AccessDeniedException("Vous n'etes pas l'enseignant assigne a cette matiere pour cette classe.");
            }
        }

        for (NoteDto.Ligne ligne : requete.notes()) {
            eleveService.obtenir(etablissementId, ligne.eleveId()); // verifie le tenant

            if (!ligne.absent()) {
                if (ligne.valeur() == null || ligne.valeur().compareTo(BigDecimal.ZERO) < 0 || ligne.valeur().compareTo(evaluation.getBareme()) > 0) {
                    throw new BusinessException("NOTE_HORS_BAREME",
                            "La note doit etre comprise entre 0 et %s (eleve %d).".formatted(evaluation.getBareme(), ligne.eleveId()),
                            HttpStatus.BAD_REQUEST);
                }
            }

            Note note = noteRepository.findByEvaluationIdAndEleveId(evaluationId, ligne.eleveId()).orElseGet(Note::new);
            note.setEvaluationId(evaluationId);
            note.setEleveId(ligne.eleveId());
            note.setValeur(ligne.absent() ? null : ligne.valeur());
            note.setAbsent(ligne.absent());
            note.setObservation(ligne.observation());
            note.setSaisiParId(principalId);
            noteRepository.save(note);
        }

        if ("planifiee".equals(evaluation.getStatut())) {
            evaluation.setStatut("saisie");
        }

        return lister(etablissementId, evaluationId);
    }

    public List<NoteDto.Profil> lister(Long etablissementId, Long evaluationId) {
        evaluationService.trouver(etablissementId, evaluationId);
        return noteRepository.findByEvaluationId(evaluationId).stream().map(n -> {
            var eleve = eleveService.obtenir(etablissementId, n.getEleveId());
            return versProfil(n, eleve.prenom() + " " + eleve.nom());
        }).toList();
    }

    private NoteDto.Profil versProfil(Note n, String eleveNom) {
        return new NoteDto.Profil(n.getId(), n.getEvaluationId(), n.getEleveId(), eleveNom, n.getValeur(),
                n.isAbsent(), n.getObservation(), n.isValide());
    }
}
