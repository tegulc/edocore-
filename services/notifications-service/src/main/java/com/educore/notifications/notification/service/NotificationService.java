package com.educore.notifications.notification.service;

import com.educore.notifications.common.BusinessException;
import com.educore.notifications.common.RessourceIntrouvableException;
import com.educore.notifications.notification.adapter.CanalNotification;
import com.educore.notifications.notification.dto.NotificationDto;
import com.educore.notifications.notification.entity.AppareilMobile;
import com.educore.notifications.notification.entity.Notification;
import com.educore.notifications.notification.entity.UtilisateurContact;
import com.educore.notifications.notification.repository.AppareilMobileRepository;
import com.educore.notifications.notification.repository.NotificationRepository;
import com.educore.notifications.notification.repository.UtilisateurContactRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Notifications (Module 24). Route vers le bon canal (sms/email/push) via le pattern adaptateur, comme les fournisseurs de paiement. */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AppareilMobileRepository appareilMobileRepository;
    private final UtilisateurContactRepository utilisateurContactRepository;
    private final Map<String, CanalNotification> canaux;

    public NotificationService(NotificationRepository notificationRepository, AppareilMobileRepository appareilMobileRepository,
                                UtilisateurContactRepository utilisateurContactRepository, List<CanalNotification> canaux) {
        this.notificationRepository = notificationRepository;
        this.appareilMobileRepository = appareilMobileRepository;
        this.utilisateurContactRepository = utilisateurContactRepository;
        this.canaux = canaux.stream().collect(Collectors.toMap(CanalNotification::code, c -> c));
    }

    @Transactional
    public NotificationDto.Profil envoyer(NotificationDto.Creation requete) {
        CanalNotification canal = canaux.get(requete.canal());
        if (canal == null) {
            throw new BusinessException("CANAL_INCONNU", "Canal de notification inconnu : " + requete.canal(), HttpStatus.BAD_REQUEST);
        }

        Notification notification = new Notification();
        notification.setEtablissementId(requete.etablissementId());
        notification.setDestinataireId(requete.destinataireId());
        notification.setCategorie(requete.categorie());
        notification.setTitre(requete.titre());
        notification.setContenu(requete.contenu());
        notification.setCanal(requete.canal());
        notificationRepository.save(notification);

        UtilisateurContact contact = utilisateurContactRepository.trouverParId(requete.destinataireId())
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur", requete.destinataireId()));
        String destinataire = "sms".equals(requete.canal()) ? contact.getTelephone() : contact.getEmail();

        boolean succes = destinataire != null && canal.envoyer(destinataire, requete.titre(), requete.contenu());
        notification.setStatutEnvoi(succes ? "envoye" : "echoue");
        notification.setEnvoyeLe(succes ? Instant.now() : null);

        return versProfil(notification);
    }

    @Transactional
    public NotificationDto.Profil marquerLue(Long notificationId, Long destinataireId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RessourceIntrouvableException("Notification", notificationId));
        if (!notification.getDestinataireId().equals(destinataireId)) {
            throw new RessourceIntrouvableException("Notification", notificationId);
        }
        notification.setLuLe(Instant.now());
        return versProfil(notification);
    }

    public List<NotificationDto.Profil> mesNotifications(Long destinataireId) {
        return notificationRepository.findByDestinataireIdOrderByCreatedAtDesc(destinataireId).stream().map(this::versProfil).toList();
    }

    @Transactional
    public void enregistrerAppareil(Long utilisateurId, NotificationDto.EnregistrementAppareil requete) {
        AppareilMobile appareil = new AppareilMobile();
        appareil.setUtilisateurId(utilisateurId);
        appareil.setPlateforme(requete.plateforme());
        appareil.setPushToken(requete.pushToken());
        appareilMobileRepository.save(appareil);
    }

    private NotificationDto.Profil versProfil(Notification n) {
        return new NotificationDto.Profil(n.getId(), n.getDestinataireId(), n.getCategorie(), n.getTitre(),
                n.getContenu(), n.getCanal(), n.getStatutEnvoi(), n.getLuLe() != null);
    }
}
