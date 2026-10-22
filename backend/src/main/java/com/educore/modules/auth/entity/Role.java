package com.educore.modules.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Role systeme (ADMIN, DIRECTEUR, CENSEUR, PROFESSEUR, ELEVE, PARENT, ...).
 * Les permissions fines de chaque role sont portees par role_permissions
 * (table associative code cote base ; a exposer via un module RBAC dedie
 * en Phase 1 avance).
 */
@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // ADMIN, DIRECTEUR, PROFESSEUR, ELEVE, PARENT, ...

    @Column(nullable = false)
    private String libelle;

    private String description;

    @Column(name = "est_systeme")
    private boolean estSysteme = true;
}
