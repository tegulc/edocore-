package com.educore.modules.parent.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "parents")
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class Parent implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(name = "utilisateur_id", nullable = false) private Long utilisateurId;
    private String profession;
    private String adresse;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
