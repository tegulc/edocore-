package com.educore.notifications.notification.adapter;

import org.springframework.stereotype.Component;

@Component
public class CanalEmail implements CanalNotification {
    @Override public String code() { return "email"; }
    @Override public boolean envoyer(String destinataire, String titre, String contenu) {
        // TODO production : appeler le fournisseur email configure (educore.integrations.email.provider-api-key)
        return true;
    }
}
