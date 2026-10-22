-- ============================================================================
-- Row-Level Security pour les Modules 13 (Discipline), 16 (Bibliotheque),
-- 17 (Infirmerie), 18 (Transport), 19 (Cantine).
-- types_sanction reste hors RLS : referentiel global (comme types_evaluation).
-- ============================================================================

-- Tables avec colonne etablissement_id directe.
ALTER TABLE livres ENABLE ROW LEVEL SECURITY;
ALTER TABLE livres FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_livres ON livres
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE bus ENABLE ROW LEVEL SECURITY;
ALTER TABLE bus FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_bus ON bus
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE menus_cantine ENABLE ROW LEVEL SECURITY;
ALTER TABLE menus_cantine FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_menus_cantine ON menus_cantine
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

-- Tables rattachees via eleve_id.
CREATE OR REPLACE FUNCTION _educore_appartient_etablissement_eleve(p_eleve_id BIGINT) RETURNS BOOLEAN AS $$
    SELECT EXISTS (
        SELECT 1 FROM eleves WHERE id = p_eleve_id AND etablissement_id = current_setting('app.etablissement_id', true)::bigint
    );
$$ LANGUAGE sql STABLE;

ALTER TABLE incidents_disciplinaires ENABLE ROW LEVEL SECURITY;
ALTER TABLE incidents_disciplinaires FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_incidents_disciplinaires ON incidents_disciplinaires USING (_educore_appartient_etablissement_eleve(eleve_id));

ALTER TABLE sanctions ENABLE ROW LEVEL SECURITY;
ALTER TABLE sanctions FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_sanctions ON sanctions USING (_educore_appartient_etablissement_eleve(eleve_id));

ALTER TABLE consultations_medicales ENABLE ROW LEVEL SECURITY;
ALTER TABLE consultations_medicales FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_consultations_medicales ON consultations_medicales USING (_educore_appartient_etablissement_eleve(eleve_id));

ALTER TABLE urgences_medicales ENABLE ROW LEVEL SECURITY;
ALTER TABLE urgences_medicales FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_urgences_medicales ON urgences_medicales USING (_educore_appartient_etablissement_eleve(eleve_id));

ALTER TABLE vaccins ENABLE ROW LEVEL SECURITY;
ALTER TABLE vaccins FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_vaccins ON vaccins USING (_educore_appartient_etablissement_eleve(eleve_id));

ALTER TABLE eleve_transport ENABLE ROW LEVEL SECURITY;
ALTER TABLE eleve_transport FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_eleve_transport ON eleve_transport USING (_educore_appartient_etablissement_eleve(eleve_id));

ALTER TABLE abonnements_cantine ENABLE ROW LEVEL SECURITY;
ALTER TABLE abonnements_cantine FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_abonnements_cantine ON abonnements_cantine USING (_educore_appartient_etablissement_eleve(eleve_id));

ALTER TABLE presences_cantine ENABLE ROW LEVEL SECURITY;
ALTER TABLE presences_cantine FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_presences_cantine ON presences_cantine USING (_educore_appartient_etablissement_eleve(eleve_id));

-- emprunts : rattachee via utilisateur_id (personnel ou eleve avec compte), pas eleve_id directement.
ALTER TABLE emprunts ENABLE ROW LEVEL SECURITY;
ALTER TABLE emprunts FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_emprunts ON emprunts
    USING (utilisateur_id IN (
        SELECT id FROM utilisateurs WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

-- itineraires/arrets_bus : rattaches via bus_id.
ALTER TABLE itineraires ENABLE ROW LEVEL SECURITY;
ALTER TABLE itineraires FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_itineraires ON itineraires
    USING (bus_id IN (
        SELECT id FROM bus WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

ALTER TABLE arrets_bus ENABLE ROW LEVEL SECURITY;
ALTER TABLE arrets_bus FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_arrets_bus ON arrets_bus
    USING (itineraire_id IN (
        SELECT i.id FROM itineraires i JOIN bus b ON b.id = i.bus_id
        WHERE b.etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));
