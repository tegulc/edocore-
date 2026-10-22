package com.educore.modules.parent.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Lien eleve <-> parent, pas de colonne etablissement_id propre (isolation via eleve_id). */
@Getter @Setter
@Entity
@Table(name = "eleve_parent", uniqueConstraints = @UniqueConstraint(columnNames = {"eleve_id", "parent_id"}))
public class EleveParent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "eleve_id", nullable = false) private Long eleveId;
    @Column(name = "parent_id", nullable = false) private Long parentId;
    @Column(name = "lien_parente", nullable = false) private String lienParente; // pere, mere, tuteur, autre
    @Column(name = "est_contact_principal", nullable = false) private boolean estContactPrincipal = false;
    @Column(name = "autorise_recuperer_eleve", nullable = false) private boolean autoriseRecupererEleve = true;
}
