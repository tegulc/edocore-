package com.educore.notifications.notification.adapter;

import org.springframework.stereotype.Component;

/** Non connecte a un vrai fournisseur SMS dans ce sandbox (pas d'acces reseau/identifiants). Simule un envoi reussi. */
@Component
public class CanalSms implements CanalNotification {
    @Override public String code() { return "sms"; }
    @Override public boolean envoyer(String destinataire, String titre, String contenu) {
        // TODO production : appeler le fournisseur SMS configure (educore.integrations.sms.provider-api-key)
        return true;
    }
}
