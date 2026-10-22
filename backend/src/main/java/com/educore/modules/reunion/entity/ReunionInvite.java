package com.educore.modules.reunion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "reunion_invites", uniqueConstraints = @UniqueConstraint(columnNames = {"reunion_id", "utilisateur_id"}))
public class ReunionInvite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "reunion_id", nullable = false) private Long reunionId;
    @Column(name = "utilisateur_id", nullable = false) private Long utilisateurId;
    @Column(name = "statut_reponse") private String statutReponse = "en_attente"; // en_attente, accepte, decline
    @Column(name = "present_effectif") private Boolean presentEffectif;
}
