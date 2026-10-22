package com.educore.modules.paiement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

/**
 * Memorise chaque Idempotency-Key traitee sur POST /paiements/initier (Volume 6,
 * Partie IV.3) : un meme client qui rejoue la meme requete (timeout reseau,
 * double-clic...) recoit la reponse du premier appel plutot que de creer un
 * second paiement pour le meme montant.
 */
@Getter @Setter
@Entity
@Table(name = "cles_idempotence", uniqueConstraints = @UniqueConstraint(columnNames = {"etablissement_id", "cle"}))
public class CleIdempotence {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(nullable = false) private String cle;
    @Column(name = "paiement_id", nullable = false) private Long paiementId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
