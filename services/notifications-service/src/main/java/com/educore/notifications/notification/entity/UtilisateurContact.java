package com.educore.notifications.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * Projection en LECTURE SEULE sur la table utilisateurs (deja geree par
 * educore-backend). Ce service ne modifie jamais cette table - il a
 * seulement besoin de l'email/telephone pour router l'envoi vers le bon
 * canal. Limite assumee de l'architecture "base partagee" (voir README) :
 * un decouplage complet exigerait de dupliquer/synchroniser ces coordonnees
 * localement (event sourcing), hors perimetre de cette premiere separation.
 */
@Getter
@Entity
@Table(name = "utilisateurs")
public class UtilisateurContact {
    @Id private Long id;
    private String email;
    private String telephone;
    @Column(name = "langue_preferee") private String languePreferee;
}
