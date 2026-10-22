-- ============================================================================
-- Row-Level Security pour le Module 6 (Emploi du temps) et le Module 12 (Presence).
-- ============================================================================

ALTER TABLE creneaux_horaires ENABLE ROW LEVEL SECURITY;
ALTER TABLE creneaux_horaires FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_creneaux_horaires ON creneaux_horaires
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE emploi_du_temps ENABLE ROW LEVEL SECURITY;
ALTER TABLE emploi_du_temps FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_emploi_du_temps ON emploi_du_temps
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

-- presences_eleves n'a pas de colonne etablissement_id propre (rattachee via eleve_id).
ALTER TABLE presences_eleves ENABLE ROW LEVEL SECURITY;
ALTER TABLE presences_eleves FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_presences_eleves ON presences_eleves
    USING (eleve_id IN (
        SELECT id FROM eleves WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));
