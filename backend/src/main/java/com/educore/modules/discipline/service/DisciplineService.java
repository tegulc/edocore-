package com.educore.modules.discipline.service;

import com.educore.common.exception.BusinessException;
import com.educore.modules.discipline.dto.DisciplineDto;
import com.educore.modules.discipline.entity.IncidentDisciplinaire;
import com.educore.modules.discipline.entity.Sanction;
import com.educore.modules.discipline.entity.TypeSanction;
import com.educore.modules.discipline.repository.IncidentDisciplinaireRepository;
import com.educore.modules.discipline.repository.SanctionRepository;
import com.educore.modules.discipline.repository.TypeSanctionRepository;
import com.educore.modules.eleve.service.EleveService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Discipline (Module 13). Regle 01 : une sanction d'exclusion (temporaire ou
 * definitive) met automatiquement a jour le statut du dossier eleve
 * (coherence avec le Module 2, ou "exclu" est un statut de premiere classe).
 */
@Service
public class DisciplineService {

    private final IncidentDisciplinaireRepository incidentRepository;
    private final SanctionRepository sanctionRepository;
    private final TypeSanctionRepository typeSanctionRepository;
    private final EleveService eleveService;

    public DisciplineService(IncidentDisciplinaireRepository incidentRepository, SanctionRepository sanctionRepository,
                              TypeSanctionRepository typeSanctionRepository, EleveService eleveService) {
        this.incidentRepository = incidentRepository;
        this.sanctionRepository = sanctionRepository;
        this.typeSanctionRepository = typeSanctionRepository;
        this.eleveService = eleveService;
    }

    @Transactional
    public DisciplineDto.IncidentProfil signaler(Long etablissementId, Long principalId, DisciplineDto.IncidentCreation requete) {
        eleveService.trouver(etablissementId, requete.eleveId());
        IncidentDisciplinaire incident = new IncidentDisciplinaire();
        incident.setEleveId(requete.eleveId());
        incident.setDateIncident(requete.dateIncident());
        incident.setDescription(requete.description());
        incident.setSignaleParId(principalId);
        incidentRepository.save(incident);
        return new DisciplineDto.IncidentProfil(incident.getId(), incident.getEleveId(), incident.getDateIncident(), incident.getDescription());
    }

    public List<DisciplineDto.IncidentProfil> historiqueIncidents(Long etablissementId, Long eleveId) {
        eleveService.trouver(etablissementId, eleveId);
        return incidentRepository.findByEleveIdOrderByDateIncidentDesc(eleveId).stream()
                .map(i -> new DisciplineDto.IncidentProfil(i.getId(), i.getEleveId(), i.getDateIncident(), i.getDescription()))
                .toList();
    }

    @Transactional
    public DisciplineDto.SanctionProfil sanctionner(Long etablissementId, Long principalId, DisciplineDto.SanctionCreation requete) {
        eleveService.trouver(etablissementId, requete.eleveId());
        TypeSanction type = typeSanctionRepository.findByCode(requete.typeSanctionCode())
                .orElseThrow(() -> new BusinessException("TYPE_SANCTION_INCONNU", "Type de sanction inconnu : " + requete.typeSanctionCode(), HttpStatus.BAD_REQUEST));

        Sanction sanction = new Sanction();
        sanction.setEleveId(requete.eleveId());
        sanction.setIncidentId(requete.incidentId());
        sanction.setTypeSanctionId(type.getId());
        sanction.setDureeJours(requete.dureeJours());
        sanction.setMotif(requete.motif());
        sanction.setDecideeParId(principalId);
        sanctionRepository.save(sanction);

        if ("exclusion_def".equals(type.getCode())) {
            eleveService.changerStatut(etablissementId, requete.eleveId(), "exclu");
        }
        // exclusion_temp : suivie via Sanction.dureeJours/dateSanction, ne change pas le statut du dossier
        // (l'eleve reste "actif" et revient en cours a l'issue de la periode d'exclusion).

        return versProfilSanction(sanction, type);
    }

    public List<DisciplineDto.SanctionProfil> historiqueSanctions(Long etablissementId, Long eleveId) {
        eleveService.trouver(etablissementId, eleveId);
        Map<Long, TypeSanction> types = typeSanctionRepository.findAll().stream().collect(Collectors.toMap(TypeSanction::getId, t -> t));
        return sanctionRepository.findByEleveIdOrderByDateSanctionDesc(eleveId).stream()
                .map(s -> versProfilSanction(s, types.get(s.getTypeSanctionId())))
                .toList();
    }

    private DisciplineDto.SanctionProfil versProfilSanction(Sanction s, TypeSanction type) {
        return new DisciplineDto.SanctionProfil(s.getId(), s.getEleveId(), s.getIncidentId(),
                type != null ? type.getCode() : null, type != null ? type.getLibelle() : null,
                s.getDateSanction(), s.getDureeJours(), s.getMotif());
    }
}
