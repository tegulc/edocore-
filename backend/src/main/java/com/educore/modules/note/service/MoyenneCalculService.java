package com.educore.modules.note.service;

import com.educore.modules.classe.dto.ClasseMatiereDto;
import com.educore.modules.classe.service.ClasseMatiereService;
import com.educore.modules.classe.service.ClasseService;
import com.educore.modules.eleve.service.EleveService;
import com.educore.modules.eleve.service.InscriptionService;
import com.educore.modules.enseignant.service.EnseignantService;
import com.educore.modules.note.dto.MoyenneDto;
import com.educore.modules.note.entity.Evaluation;
import com.educore.modules.note.entity.MoyenneGenerale;
import com.educore.modules.note.entity.MoyenneMatiere;
import com.educore.modules.note.entity.Note;
import com.educore.modules.note.entity.TypeEvaluation;
import com.educore.modules.note.repository.EvaluationRepository;
import com.educore.modules.note.repository.MoyenneGeneraleRepository;
import com.educore.modules.note.repository.MoyenneMatiereRepository;
import com.educore.modules.note.repository.NoteRepository;
import com.educore.modules.note.repository.TypeEvaluationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Moteur de calcul des moyennes - systeme francophone classique (notes /20,
 * moyenne ponderee par coefficient). Se declenche explicitement (action
 * "recalculer les moyennes de la periode"), pas automatiquement a chaque
 * note saisie : les moyennes ne doivent refleter que les evaluations
 * VALIDEES, et un recalcul en cascade a chaque saisie serait couteux et
 * prematurement instable pendant que les enseignants saisissent encore.
 *
 * Moyenne matiere = somme(note x ponderation_type) / somme(ponderation_type),
 * sur les evaluations validees uniquement.
 * Moyenne generale = somme(moyenne_matiere x coefficient_matiere) / somme(coefficient),
 * puis rang = position dans le classement de la classe pour la periode.
 */
@Service
public class MoyenneCalculService {

    private static final MathContext PRECISION = new MathContext(4);

    private final EvaluationRepository evaluationRepository;
    private final NoteRepository noteRepository;
    private final TypeEvaluationRepository typeEvaluationRepository;
    private final MoyenneMatiereRepository moyenneMatiereRepository;
    private final MoyenneGeneraleRepository moyenneGeneraleRepository;
    private final ClasseMatiereService classeMatiereService;
    private final InscriptionService inscriptionService;
    private final EleveService eleveService;
    private final EnseignantService enseignantService;

    public MoyenneCalculService(EvaluationRepository evaluationRepository, NoteRepository noteRepository,
                                 TypeEvaluationRepository typeEvaluationRepository, MoyenneMatiereRepository moyenneMatiereRepository,
                                 MoyenneGeneraleRepository moyenneGeneraleRepository, ClasseMatiereService classeMatiereService,
                                 InscriptionService inscriptionService, EleveService eleveService,
                                 EnseignantService enseignantService) {
        this.evaluationRepository = evaluationRepository;
        this.noteRepository = noteRepository;
        this.typeEvaluationRepository = typeEvaluationRepository;
        this.moyenneMatiereRepository = moyenneMatiereRepository;
        this.moyenneGeneraleRepository = moyenneGeneraleRepository;
        this.classeMatiereService = classeMatiereService;
        this.inscriptionService = inscriptionService;
        this.eleveService = eleveService;
        this.enseignantService = enseignantService;
    }

    @Transactional
    public List<MoyenneDto.Generale> recalculerPourClasse(Long etablissementId, Long classeId, Long periodeId) {
        List<Long> eleveIds = inscriptionService.listerElevesInscrits(classeId);
        List<ClasseMatiereDto.Profil> matieres = classeMatiereService.lister(etablissementId, classeId);
        Map<Long, BigDecimal> ponderationsParType = typeEvaluationRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(TypeEvaluation::getId, TypeEvaluation::getPonderation));

        List<MoyenneDto.Generale> resultats = new ArrayList<>();

        for (Long eleveId : eleveIds) {
            BigDecimal sommeMoyennesPonderees = BigDecimal.ZERO;
            BigDecimal sommeCoefficients = BigDecimal.ZERO;

            for (ClasseMatiereDto.Profil matiere : matieres) {
                BigDecimal moyenneMatiere = calculerMoyenneMatiere(eleveId, matiere.id(), periodeId, ponderationsParType);
                if (moyenneMatiere != null) {
                    enregistrerMoyenneMatiere(eleveId, matiere.id(), periodeId, moyenneMatiere);
                    sommeMoyennesPonderees = sommeMoyennesPonderees.add(moyenneMatiere.multiply(matiere.coefficient()));
                    sommeCoefficients = sommeCoefficients.add(matiere.coefficient());
                }
            }

            BigDecimal moyenneGenerale = sommeCoefficients.compareTo(BigDecimal.ZERO) > 0
                    ? sommeMoyennesPonderees.divide(sommeCoefficients, PRECISION)
                    : null;

            MoyenneGenerale entite = moyenneGeneraleRepository.findByEleveIdAndPeriodeId(eleveId, periodeId).orElseGet(MoyenneGenerale::new);
            entite.setEleveId(eleveId);
            entite.setClasseId(classeId);
            entite.setPeriodeId(periodeId);
            entite.setMoyenneGenerale(moyenneGenerale);
            entite.setDecision(deciderMention(moyenneGenerale));
            moyenneGeneraleRepository.save(entite);

            var eleve = eleveService.obtenir(etablissementId, eleveId);
            resultats.add(new MoyenneDto.Generale(eleveId, eleve.prenom() + " " + eleve.nom(), moyenneGenerale, null, entite.getDecision()));
        }

        return attribuerRangs(classeId, periodeId, resultats);
    }

    @Transactional
    public Map<Long, StatistiquesMatiere> calculerStatistiquesClasse(Long etablissementId, Long classeId, Long periodeId) {
        List<ClasseMatiereDto.Profil> matieres = classeMatiereService.lister(etablissementId, classeId);
        Map<Long, StatistiquesMatiere> statistiques = new HashMap<>();

        for (ClasseMatiereDto.Profil matiere : matieres) {
            List<MoyenneMatiere> moyennesMatiere = moyenneMatiereRepository.findByClasseMatiereIdAndPeriodeId(matiere.id(), periodeId);
            
            if (!moyennesMatiere.isEmpty()) {
                BigDecimal max = moyennesMatiere.stream()
                        .map(MoyenneMatiere::getMoyenne)
                        .filter(m -> m != null)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                
                BigDecimal min = moyennesMatiere.stream()
                        .map(MoyenneMatiere::getMoyenne)
                        .filter(m -> m != null)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                
                BigDecimal moyenneClasse = moyennesMatiere.stream()
                        .map(MoyenneMatiere::getMoyenne)
                        .filter(m -> m != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(moyennesMatiere.size()), PRECISION);
                
                statistiques.put(matiere.id(), new StatistiquesMatiere(max, min, moyenneClasse));
            }
        }

        return statistiques;
    }

    private BigDecimal calculerMoyenneMatiere(Long eleveId, Long classeMatiereId, Long periodeId, Map<Long, BigDecimal> ponderations) {
        List<Note> notesValidees = noteRepository.trouverNotesValideesPourMatierePeriode(eleveId, classeMatiereId, periodeId);
        if (notesValidees.isEmpty()) return null;

        BigDecimal sommePonderee = BigDecimal.ZERO;
        BigDecimal sommePonderations = BigDecimal.ZERO;

        for (Note note : notesValidees) {
            if (note.isAbsent() || note.getValeur() == null) continue; // absence non chiffree : n'entre pas dans le calcul
            Evaluation evaluation = evaluationRepository.findById(note.getEvaluationId()).orElse(null);
            if (evaluation == null) continue;
            BigDecimal ponderation = ponderations.getOrDefault(evaluation.getTypeEvaluationId(), BigDecimal.ONE);
            // Ramene la note sur 20 si le bareme de l'evaluation differe (ex: note sur 10 -> /20).
            BigDecimal noteSur20 = note.getValeur().multiply(BigDecimal.valueOf(20)).divide(evaluation.getBareme(), PRECISION);
            sommePonderee = sommePonderee.add(noteSur20.multiply(ponderation));
            sommePonderations = sommePonderations.add(ponderation);
        }

        return sommePonderations.compareTo(BigDecimal.ZERO) > 0 ? sommePonderee.divide(sommePonderations, PRECISION) : null;
    }

    private void enregistrerMoyenneMatiere(Long eleveId, Long classeMatiereId, Long periodeId, BigDecimal moyenne) {
        MoyenneMatiere entite = moyenneMatiereRepository.findByEleveIdAndClasseMatiereIdAndPeriodeId(eleveId, classeMatiereId, periodeId)
                .orElseGet(MoyenneMatiere::new);
        entite.setEleveId(eleveId);
        entite.setClasseMatiereId(classeMatiereId);
        entite.setPeriodeId(periodeId);
        entite.setMoyenne(moyenne);
        moyenneMatiereRepository.save(entite);
    }

    private List<MoyenneDto.Generale> attribuerRangs(Long classeId, Long periodeId, List<MoyenneDto.Generale> resultats) {
        List<MoyenneGenerale> classementBrut = moyenneGeneraleRepository.findByClasseIdAndPeriodeIdOrderByMoyenneGeneraleDesc(classeId, periodeId);
        Map<Long, Integer> rangParEleve = new HashMap<>();
        int rang = 1;
        for (MoyenneGenerale mg : classementBrut) {
            if (mg.getMoyenneGenerale() == null) continue;
            rangParEleve.put(mg.getEleveId(), rang++);
            mg.setRangClasse(rangParEleve.get(mg.getEleveId()));
        }
        return resultats.stream()
                .map(r -> new MoyenneDto.Generale(r.eleveId(), r.eleveNom(), r.moyenneGenerale(), rangParEleve.get(r.eleveId()), r.decision()))
                .sorted(Comparator.comparing(MoyenneDto.Generale::rangClasse, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private String deciderMention(BigDecimal moyenne) {
        if (moyenne == null) return null;
        if (moyenne.compareTo(BigDecimal.valueOf(16)) >= 0) return "felicitations";
        if (moyenne.compareTo(BigDecimal.valueOf(14)) >= 0) return "encouragements";
        if (moyenne.compareTo(BigDecimal.valueOf(8)) < 0) return "avertissement";
        return null;
    }

    public String obtenirAppreciationMatiere(BigDecimal moyenne) {
        if (moyenne == null) return "—";
        if (moyenne.compareTo(BigDecimal.valueOf(16)) >= 0) return "Excellent travail";
        if (moyenne.compareTo(BigDecimal.valueOf(14)) >= 0) return "Très bon travail";
        if (moyenne.compareTo(BigDecimal.valueOf(12)) >= 0) return "Bon travail";
        if (moyenne.compareTo(BigDecimal.valueOf(10)) >= 0) return "Travail satisfaisant";
        if (moyenne.compareTo(BigDecimal.valueOf(8)) >= 0) return "Travail améliorable";
        if (moyenne.compareTo(BigDecimal.valueOf(5)) >= 0) return "Travail insuffisant";
        return "Très insuffisant";
    }

    public record StatistiquesMatiere(BigDecimal max, BigDecimal min, BigDecimal moyenneClasse) {}
}
