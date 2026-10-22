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
 * Permission fine (ex: NOTES_SAISIR, NOTES_VALIDER, ELEVE_CREER).
 * Un role = un ensemble de permissions (table associative role_permissions).
 * Ce niveau de granularite permet de distinguer, par exemple, un professeur
 * habilite a SAISIR des notes d'un chef de departement habilite a les VALIDER.
 */
@Getter
@Setter
@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String module;

    @Column(nullable = false)
    private String libelle;

    private String description;
}
