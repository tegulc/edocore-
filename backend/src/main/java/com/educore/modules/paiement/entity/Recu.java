package com.educore.modules.paiement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "recus")
public class Recu {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "paiement_id", nullable = false) private Long paiementId;
    @Column(name = "numero_recu", nullable = false, unique = true) private String numeroRecu;
    @Column(name = "fichier_pdf_url") private String fichierPdfUrl;
    @Column(name = "genere_le", nullable = false) private Instant genereLe = Instant.now();
}
