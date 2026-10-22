package com.educore.modules.reunion.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "reunions")
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class Reunion implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id", nullable = false) private Long etablissementId;
    @Column(nullable = false) private String titre;
    private String description;
    @Column(name = "date_reunion", nullable = false) private Instant dateReunion;
    private String lieu;
    @Column(name = "lien_visioconference") private String lienVisioconference;
    @Column(name = "organisateur_id") private Long organisateurId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
