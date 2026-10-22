-- ============================================================================
-- ROW-LEVEL SECURITY POUR LA STRUCTURE DU SYSTÈME ÉDUCATIF
-- ============================================================================
-- Application des politiques RLS sur les nouvelles tables de la structure
-- du système éducatif camerounais
-- ============================================================================

-- Créer le rôle educore_app s'il n'existe pas
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'educore_app') THEN
        CREATE ROLE educore_app;
    END IF;
END
$$;

-- Politique RLS pour la table matiere_niveau_serie
CREATE POLICY rls_matiere_niveau_serie ON matiere_niveau_serie
    FOR ALL TO educore_app
    USING (etablissement_id = current_setting('app.etablissement_id')::BIGINT);

ALTER TABLE matiere_niveau_serie ENABLE ROW LEVEL SECURITY;
ALTER TABLE matiere_niveau_serie FORCE ROW LEVEL SECURITY;

-- Politique RLS pour la table etablissement_langue_option
CREATE POLICY rls_etablissement_langue_option ON etablissement_langue_option
    FOR ALL TO educore_app
    USING (etablissement_id = current_setting('app.etablissement_id')::BIGINT);

ALTER TABLE etablissement_langue_option ENABLE ROW LEVEL SECURITY;
ALTER TABLE etablissement_langue_option FORCE ROW LEVEL SECURITY;

-- Les tables sous_systemes et cycles sont des tables de référence globales
-- et ne nécessitent pas de RLS (elles sont partagées par tous les établissements)
