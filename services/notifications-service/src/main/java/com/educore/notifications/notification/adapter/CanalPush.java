package com.educore.notifications.notification.adapter;

import org.springframework.stereotype.Component;

/** Cible les push_token enregistres dans appareils_mobiles. Non connecte a Firebase Cloud Messaging dans ce sandbox. */
@Component
public class CanalPush implements CanalNotification {
    @Override public String code() { return "push"; }
    @Override public boolean envoyer(String destinataire, String titre, String contenu) {
        // TODO production : appeler Firebase Cloud Messaging (educore.integrations.push.firebase-server-key)
        return true;
    }
}
