package com.educore.modules.infirmerie.service;

import com.educore.modules.eleve.service.EleveService;
import com.educore.modules.infirmerie.dto.InfirmerieDto;
import com.educore.modules.infirmerie.entity.ConsultationMedicale;
import com.educore.modules.infirmerie.entity.UrgenceMedicale;
import com.educore.modules.infirmerie.entity.Vaccin;
import com.educore.modules.infirmerie.repository.ConsultationMedicaleRepository;
import com.educore.modules.infirmerie.repository.UrgenceMedicaleRepository;
import com.educore.modules.infirmerie.repository.VaccinRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

/** Infirmerie (Module 17). Les urgences medicales sont pensees pour declencher une notification parent immediate (Module 24, a venir). */
@Service
public class InfirmerieService {

    private final ConsultationMedicaleRepository consultationRepository;
    private final UrgenceMedicaleRepository urgenceRepository;
    private final VaccinRepository vaccinRepository;
    private final EleveService eleveService;

    public InfirmerieService(ConsultationMedicaleRepository consultationRepository, UrgenceMedicaleRepository urgenceRepository,
                              VaccinRepository vaccinRepository, EleveService eleveService) {
        this.consultationRepository = consultationRepository;
        this.urgenceRepository = urgenceRepository;
        this.vaccinRepository = vaccinRepository;
        this.eleveService = eleveService;
    }

    @Transactional
    public InfirmerieDto.ConsultationProfil consulter(Long etablissementId, Long principalId, InfirmerieDto.ConsultationCreation requete) {
        eleveService.trouver(etablissementId, requete.eleveId());
        ConsultationMedicale c = new ConsultationMedicale();
        c.setEleveId(requete.eleveId());
        c.setMotif(requete.motif());
        c.setDiagnostic(requete.diagnostic());
        c.setTraitement(requete.traitement());
        c.setInfirmierId(principalId);
        consultationRepository.save(c);
        return versProfilConsultation(c);
    }

    public List<InfirmerieDto.ConsultationProfil> historiqueConsultations(Long etablissementId, Long eleveId) {
        eleveService.trouver(etablissementId, eleveId);
        return consultationRepository.findByEleveIdOrderByDateConsultationDesc(eleveId).stream().map(this::versProfilConsultation).toList();
    }

    @Transactional
    public InfirmerieDto.UrgenceProfil declarerUrgence(Long etablissementId, InfirmerieDto.UrgenceCreation requete) {
        eleveService.trouver(etablissementId, requete.eleveId());
        UrgenceMedicale u = new UrgenceMedicale();
        u.setEleveId(requete.eleveId());
        u.setDescription(requete.description());
        u.setActionPrise(requete.actionPrise());
        u.setHopitalTransfert(requete.hopitalTransfert());
        // TODO Module 24 (Notifications) : notifier immediatement le(s) parent(s), puis marquer parentNotifie=true.
        urgenceRepository.save(u);
        return versProfilUrgence(u);
    }

    public List<InfirmerieDto.UrgenceProfil> historiqueUrgences(Long etablissementId, Long eleveId) {
        eleveService.trouver(etablissementId, eleveId);
        return urgenceRepository.findByEleveIdOrderByDateUrgenceDesc(eleveId).stream().map(this::versProfilUrgence).toList();
    }

    @Transactional
    public InfirmerieDto.VaccinProfil enregistrerVaccin(Long etablissementId, InfirmerieDto.VaccinCreation requete) {
        eleveService.trouver(etablissementId, requete.eleveId());
        Vaccin v = new Vaccin();
        v.setEleveId(requete.eleveId());
        v.setNomVaccin(requete.nomVaccin());
        v.setDateAdministration(requete.dateAdministration());
        v.setRappelPrevu(requete.rappelPrevu());
        vaccinRepository.save(v);
        return new InfirmerieDto.VaccinProfil(v.getId(), v.getEleveId(), v.getNomVaccin(), v.getDateAdministration(), v.getRappelPrevu());
    }

    public List<InfirmerieDto.VaccinProfil> historiqueVaccins(Long etablissementId, Long eleveId) {
        eleveService.trouver(etablissementId, eleveId);
        return vaccinRepository.findByEleveIdOrderByDateAdministrationDesc(eleveId).stream()
                .map(v -> new InfirmerieDto.VaccinProfil(v.getId(), v.getEleveId(), v.getNomVaccin(), v.getDateAdministration(), v.getRappelPrevu()))
                .toList();
    }

    private InfirmerieDto.ConsultationProfil versProfilConsultation(ConsultationMedicale c) {
        return new InfirmerieDto.ConsultationProfil(c.getId(), c.getEleveId(),
                DateTimeFormatter.ISO_INSTANT.format(c.getDateConsultation()), c.getMotif(), c.getDiagnostic(), c.getTraitement());
    }

    private InfirmerieDto.UrgenceProfil versProfilUrgence(UrgenceMedicale u) {
        return new InfirmerieDto.UrgenceProfil(u.getId(), u.getEleveId(), DateTimeFormatter.ISO_INSTANT.format(u.getDateUrgence()),
                u.getDescription(), u.getActionPrise(), u.getHopitalTransfert(), u.isParentNotifie());
    }
}
