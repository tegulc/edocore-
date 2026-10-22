package com.educore.common.base;

/**
 * Contrat impose a toute entite appartenant a un etablissement (tenant).
 * Garantit qu'aucune requete ne peut, par erreur, croiser les donnees
 * de deux etablissements differents (isolation multi-tenant stricte).
 *
 * Applique en pratique via un filtre Hibernate global (@FilterDef) configure
 * dans TenantConfig, active automatiquement selon l'etablissement_id du
 * contexte de securite courant (utilisateur connecte).
 */
public interface TenantScoped {
    Long getEtablissementId();
    void setEtablissementId(Long etablissementId);
}
