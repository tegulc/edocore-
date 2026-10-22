package com.educore.notifications.notification.adapter;

/** Meme pattern adaptateur que ProviderPaiement (Phase 5) - ajouter un canal = ajouter une implementation. */
public interface CanalNotification {
    String code(); // sms, email, push
    boolean envoyer(String destinataire, String titre, String contenu);
}
