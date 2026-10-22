-- ============================================================================
-- EDUCORE / SIGES - Row-Level Security (isolation multi-tenant, niveau base)
-- Conforme au Volume 2 (Cahier des specifications techniques) §2.3 et au
-- Volume 3 (Architecture logicielle) Partie VII : le filtrage applicatif
-- (Hibernate @Filter + TenantContext) reste la premiere ligne de defense,
-- RLS est le "filet de securite ultime" qui protege meme en cas d'erreur
-- applicative (requete native mal filtree, bug, etc.).
--
-- Mecanisme : chaque politique compare etablissement_id (ou l'etablissement_id
-- de la ligne parente, via sous-requete quand la colonne n'est pas directe) a
-- la variable de session app.etablissement_id, positionnee par le backend a
-- chaque transaction via set_config('app.etablissement_id', ..., true)
-- (voir TenantRlsAspect, portee "true" = reinitialisee automatiquement a
-- chaque fin de transaction, compatible avec le pooling de connexions).
--
-- Deploiement incremental : seules les tables deja modelisees cote backend
-- (Modules 1 et le socle multi-tenant/annees scolaires de cette phase) sont
-- couvertes ici. Chaque futur module ajoute sa propre politique dans sa
-- propre migration, au moment ou il est livre, plutot qu'une seule migration
-- monolithique sur les ~90 tables du schema complet.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- etablissements : tenant racine, pas de colonne etablissement_id (c'est lui-
-- meme le tenant). La politique compare directement l'id de la ligne.
-- ----------------------------------------------------------------------------
ALTER TABLE etablissements ENABLE ROW LEVEL SECURITY;
ALTER TABLE etablissements FORCE ROW LEVEL SECURITY;

CREATE POLICY isolation_etablissements ON etablissements
    USING (id = current_setting('app.etablissement_id', true)::bigint);

-- ----------------------------------------------------------------------------
-- Tables avec colonne etablissement_id directe : politique simple.
-- ----------------------------------------------------------------------------
ALTER TABLE utilisateurs ENABLE ROW LEVEL SECURITY;
ALTER TABLE utilisateurs FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_utilisateurs ON utilisateurs
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE annees_scolaires ENABLE ROW LEVEL SECURITY;
ALTER TABLE annees_scolaires FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_annees_scolaires ON annees_scolaires
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE jours_feries ENABLE ROW LEVEL SECURITY;
ALTER TABLE jours_feries FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_jours_feries ON jours_feries
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

-- journal_audit.etablissement_id est nullable (une action systeme sans contexte
-- d'etablissement reste possible) : on laisse passer ces lignes-la (IS NULL),
-- sinon elles deviendraient invisibles a tout le monde y compris un superviseur.
ALTER TABLE journal_audit ENABLE ROW LEVEL SECURITY;
ALTER TABLE journal_audit FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_journal_audit ON journal_audit
    USING (etablissement_id IS NULL OR etablissement_id = current_setting('app.etablissement_id', true)::bigint);

-- ----------------------------------------------------------------------------
-- Tables rattachees indirectement (pas de colonne etablissement_id propre) :
-- politique par sous-requete vers la table parente.
-- ----------------------------------------------------------------------------
ALTER TABLE periodes ENABLE ROW LEVEL SECURITY;
ALTER TABLE periodes FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_periodes ON periodes
    USING (annee_scolaire_id IN (
        SELECT id FROM annees_scolaires WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

ALTER TABLE vacances_scolaires ENABLE ROW LEVEL SECURITY;
ALTER TABLE vacances_scolaires FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_vacances_scolaires ON vacances_scolaires
    USING (annee_scolaire_id IN (
        SELECT id FROM annees_scolaires WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

ALTER TABLE utilisateur_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE utilisateur_sessions FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_utilisateur_sessions ON utilisateur_sessions
    USING (utilisateur_id IN (
        SELECT id FROM utilisateurs WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

ALTER TABLE historique_connexions ENABLE ROW LEVEL SECURITY;
ALTER TABLE historique_connexions FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_historique_connexions ON historique_connexions
    USING (utilisateur_id IN (
        SELECT id FROM utilisateurs WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

-- ----------------------------------------------------------------------------
-- roles / permissions / role_permissions / utilisateur_roles : referentiel
-- RBAC PARTAGE entre tous les etablissements (les 15 roles systeme et leurs
-- permissions sont globaux, pas dupliques par tenant - voir Volume 1 Module 1).
-- Aucune politique RLS ici volontairement : ce ne sont pas des donnees tenant.
-- utilisateur_roles fait exception (associe un utilisateur, donc un tenant,
-- a un role global) et merite sa propre politique par sous-requete :
-- ----------------------------------------------------------------------------
ALTER TABLE utilisateur_roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE utilisateur_roles FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_utilisateur_roles ON utilisateur_roles
    USING (utilisateur_id IN (
        SELECT id FROM utilisateurs WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));
