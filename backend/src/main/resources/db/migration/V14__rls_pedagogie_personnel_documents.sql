-- ============================================================================
-- Row-Level Security pour Modules 10/11 (Cahier de texte, Devoirs),
-- Module 20 (Personnel), Module 21 (Documents).
-- ============================================================================

CREATE OR REPLACE FUNCTION _educore_appartient_etablissement_classe_matiere(p_cm_id BIGINT) RETURNS BOOLEAN AS $$
    SELECT EXISTS (
        SELECT 1 FROM classe_matiere cm
        JOIN classes c ON c.id = cm.classe_id
        WHERE cm.id = p_cm_id
          AND c.etablissement_id = current_setting('app.etablissement_id', true)::bigint
    );
$$ LANGUAGE sql STABLE;

ALTER TABLE cahier_de_texte ENABLE ROW LEVEL SECURITY;
ALTER TABLE cahier_de_texte FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_cahier_de_texte ON cahier_de_texte
    USING (_educore_appartient_etablissement_classe_matiere(classe_matiere_id));

ALTER TABLE devoirs ENABLE ROW LEVEL SECURITY;
ALTER TABLE devoirs FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_devoirs ON devoirs
    USING (_educore_appartient_etablissement_classe_matiere(classe_matiere_id));

ALTER TABLE devoirs_rendus ENABLE ROW LEVEL SECURITY;
ALTER TABLE devoirs_rendus FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_devoirs_rendus ON devoirs_rendus
    USING (_educore_appartient_etablissement_eleve(eleve_id));

ALTER TABLE personnel ENABLE ROW LEVEL SECURITY;
ALTER TABLE personnel FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_personnel ON personnel
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE documents ENABLE ROW LEVEL SECURITY;
ALTER TABLE documents FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_documents ON documents
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE demandes_conges ENABLE ROW LEVEL SECURITY;
ALTER TABLE demandes_conges FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_demandes_conges ON demandes_conges
    USING (utilisateur_id IN (
        SELECT id FROM utilisateurs WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

ALTER TABLE contrats ENABLE ROW LEVEL SECURITY;
ALTER TABLE contrats FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_contrats ON contrats
    USING (utilisateur_id IN (
        SELECT id FROM utilisateurs WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

ALTER TABLE heures_travail ENABLE ROW LEVEL SECURITY;
ALTER TABLE heures_travail FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_heures_travail ON heures_travail
    USING (utilisateur_id IN (
        SELECT id FROM utilisateurs WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

ALTER TABLE salaires ENABLE ROW LEVEL SECURITY;
ALTER TABLE salaires FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_salaires ON salaires
    USING (utilisateur_id IN (
        SELECT id FROM utilisateurs WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));
