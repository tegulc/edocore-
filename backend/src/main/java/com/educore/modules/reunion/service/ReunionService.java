package com.educore.modules.reunion.service;

import com.educore.common.exception.RessourceIntrouvableException;
import com.educore.common.integration.NotificationClient;
import com.educore.modules.reunion.dto.ReunionDto;
import com.educore.modules.reunion.entity.CompteRendu;
import com.educore.modules.reunion.entity.Reunion;
import com.educore.modules.reunion.entity.ReunionInvite;
import com.educore.modules.reunion.repository.CompteRenduRepository;
import com.educore.modules.reunion.repository.ReunionInviteRepository;
import com.educore.modules.reunion.repository.ReunionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

/** Reunions (Module 23) - reste dans le monolithe modulaire, a la difference de Chat/Notifications (voir document d'architecture Phase 7). */
@Service
public class ReunionService {

    private final ReunionRepository reunionRepository;
    private final ReunionInviteRepository reunionInviteRepository;
    private final CompteRenduRepository compteRenduRepository;
    private final NotificationClient notificationClient;

    public ReunionService(ReunionRepository reunionRepository, ReunionInviteRepository reunionInviteRepository,
                           CompteRenduRepository compteRenduRepository, NotificationClient notificationClient) {
        this.reunionRepository = reunionRepository;
        this.reunionInviteRepository = reunionInviteRepository;
        this.compteRenduRepository = compteRenduRepository;
        this.notificationClient = notificationClient;
    }

    @Transactional
    public ReunionDto.Profil creer(Long etablissementId, Long organisateurId, ReunionDto.Creation requete) {
        Reunion reunion = new Reunion();
        reunion.setEtablissementId(etablissementId);
        reunion.setTitre(requete.titre());
        reunion.setDescription(requete.description());
        reunion.setDateReunion(requete.dateReunion());
        reunion.setLieu(requete.lieu());
        reunion.setLienVisioconference(requete.lienVisioconference());
        reunion.setOrganisateurId(organisateurId);
        reunionRepository.save(reunion);

        requete.invitesIds().forEach(utilisateurId -> {
            ReunionInvite invite = new ReunionInvite();
            invite.setReunionId(reunion.getId());
            invite.setUtilisateurId(utilisateurId);
            reunionInviteRepository.save(invite);
            notificationClient.envoyer(etablissementId, utilisateurId, "reunion",
                    "Invitation : " + requete.titre(),
                    "Vous etes invite a une reunion le " + DateTimeFormatter.ISO_INSTANT.format(requete.dateReunion()), "sms");
        });

        return versProfil(reunion);
    }

    public List<ReunionDto.Profil> lister(Long etablissementId) {
        return reunionRepository.findByEtablissementIdOrderByDateReunionDesc(etablissementId).stream().map(this::versProfil).toList();
    }

    @Transactional
    public ReunionDto.InviteProfil repondre(Long reunionId, Long utilisateurId, ReunionDto.Reponse requete) {
        ReunionInvite invite = reunionInviteRepository.findByReunionIdAndUtilisateurId(reunionId, utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("ReunionInvite", reunionId));
        invite.setStatutReponse(requete.statutReponse());
        return new ReunionDto.InviteProfil(invite.getUtilisateurId(), invite.getStatutReponse(), invite.getPresentEffectif());
    }

    public List<ReunionDto.InviteProfil> invites(Long reunionId) {
        return reunionInviteRepository.findByReunionId(reunionId).stream()
                .map(i -> new ReunionDto.InviteProfil(i.getUtilisateurId(), i.getStatutReponse(), i.getPresentEffectif()))
                .toList();
    }

    @Transactional
    public ReunionDto.CompteRenduProfil rediger(Long reunionId, Long redacteurId, ReunionDto.CompteRenduCreation requete) {
        CompteRendu compteRendu = compteRenduRepository.findByReunionId(reunionId).orElseGet(CompteRendu::new);
        compteRendu.setReunionId(reunionId);
        compteRendu.setContenu(requete.contenu());
        compteRendu.setFichierUrl(requete.fichierUrl());
        compteRendu.setRedigeParId(redacteurId);
        compteRenduRepository.save(compteRendu);
        return new ReunionDto.CompteRenduProfil(compteRendu.getId(), reunionId, compteRendu.getContenu(), compteRendu.getFichierUrl());
    }

    private ReunionDto.Profil versProfil(Reunion r) {
        return new ReunionDto.Profil(r.getId(), r.getTitre(), r.getDescription(), r.getDateReunion(),
                r.getLieu(), r.getLienVisioconference(), r.getOrganisateurId());
    }
}
