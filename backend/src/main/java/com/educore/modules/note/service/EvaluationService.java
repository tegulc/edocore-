package com.educore.modules.note.service;

import com.educore.common.exception.BusinessException;
import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.modules.classe.service.ClasseMatiereService;
import com.educore.modules.note.dto.EvaluationDto;
import com.educore.modules.note.entity.Evaluation;
import com.educore.modules.note.entity.TypeEvaluation;
import com.educore.modules.note.repository.EvaluationRepository;
import com.educore.modules.note.repository.TypeEvaluationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Devoirs/compositions/examens (Module 8). Workflow : planifiee -> saisie ->
 * validee (verrouillee, plus aucune note modifiable) -> publiee.
 */
@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final TypeEvaluationRepository typeEvaluationRepository;
    private final ClasseMatiereService classeMatiereService;

    public EvaluationService(EvaluationRepository evaluationRepository, TypeEvaluationRepository typeEvaluationRepository,
                              ClasseMatiereService classeMatiereService) {
        this.evaluationRepository = evaluationRepository;
        this.typeEvaluationRepository = typeEvaluationRepository;
        this.classeMatiereService = classeMatiereService;
    }

    @Transactional
    public EvaluationDto.Profil creer(Long etablissementId, Long principalId, EvaluationDto.Creation requete) {
        classeMatiereService.obtenir(etablissementId, requete.classeMatiereId()); // verifie le tenant

        TypeEvaluation type = typeEvaluationRepository.findByCode(requete.typeEvaluationCode())
                .orElseThrow(() -> new BusinessException("TYPE_EVALUATION_INCONNU", "Type d'evaluation inconnu : " + requete.typeEvaluationCode(), HttpStatus.BAD_REQUEST));

        Evaluation evaluation = new Evaluation();
        evaluation.setEtablissementId(etablissementId);
        evaluation.setClasseMatiereId(requete.classeMatiereId());
        evaluation.setTypeEvaluationId(type.getId());
        evaluation.setPeriodeId(requete.periodeId());
        evaluation.setTitre(requete.titre());
        evaluation.setDateEvaluation(requete.dateEvaluation());
        if (requete.bareme() != null) evaluation.setBareme(requete.bareme());
        evaluation.setCreatedById(principalId);
        evaluationRepository.save(evaluation);

        return versProfil(evaluation, type.getCode());
    }

    @Transactional
    public void valider(Long etablissementId, Long evaluationId) {
        Evaluation evaluation = trouver(etablissementId, evaluationId);
        if ("validee".equals(evaluation.getStatut()) || "publiee".equals(evaluation.getStatut())) {
            throw new BusinessException("EVALUATION_DEJA_VALIDEE", "Cette evaluation est deja validee.", HttpStatus.CONFLICT);
        }
        evaluation.setStatut("validee");
    }

    public List<EvaluationDto.Profil> listerParClasseMatiereEtPeriode(Long etablissementId, Long classeMatiereId, Long periodeId) {
        classeMatiereService.obtenir(etablissementId, classeMatiereId);
        Map<Long, String> codesTypes = typeEvaluationRepository.findAll().stream()
                .collect(Collectors.toMap(TypeEvaluation::getId, TypeEvaluation::getCode));
        return evaluationRepository.findByClasseMatiereIdAndPeriodeId(classeMatiereId, periodeId).stream()
                .map(e -> versProfil(e, codesTypes.get(e.getTypeEvaluationId())))
                .toList();
    }

    Evaluation trouver(Long etablissementId, Long evaluationId) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RessourceIntrouvableException("Evaluation", evaluationId));
        if (!evaluation.getEtablissementId().equals(etablissementId)) {
            throw new RessourceIntrouvableException("Evaluation", evaluationId);
        }
        return evaluation;
    }

    private EvaluationDto.Profil versProfil(Evaluation e, String typeCode) {
        return new EvaluationDto.Profil(e.getId(), e.getClasseMatiereId(), typeCode, e.getPeriodeId(),
                e.getTitre(), e.getDateEvaluation(), e.getBareme(), e.getStatut());
    }
}
