package com.educore.modules.apipublique.entity;

import com.educore.common.base.TenantScoped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.time.Instant;

@Getter @Setter
@Entity
@Table(name = "api_clients")
@Filter(name = "filtreTenant", condition = "etablissement_id = :etablissementId")
public class ApiClient implements TenantScoped {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "etablissement_id") private Long etablissementId;
    @Column(nullable = false) private String nom;
    @Column(name = "client_id", nullable = false, unique = true) private String clientId;
    @Column(name = "client_secret_hash", nullable = false) private String clientSecretHash;
    private String statut = "actif";
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
