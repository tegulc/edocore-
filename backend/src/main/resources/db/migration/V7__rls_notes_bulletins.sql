-- ============================================================================
-- Row-Level Security pour le Module 8 (Notes, Moyennes, Bulletins).
-- types_evaluation reste hors RLS : referentiel global, comme roles/permissions.
-- ============================================================================

ALTER TABLE evaluations ENABLE ROW LEVEL SECURITY;
ALTER TABLE evaluations FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_evaluations ON evaluations
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

-- notes, moyennes_matiere, moyennes_generales, bulletins n'ont pas de colonne
-- etablissement_id propre : isolation via eleve_id (deja protege via eleves).
ALTER TABLE notes ENABLE ROW LEVEL SECURITY;
ALTER TABLE notes FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_notes ON notes
    USING (eleve_id IN (
        SELECT id FROM eleves WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

ALTER TABLE moyennes_matiere ENABLE ROW LEVEL SECURITY;
ALTER TABLE moyennes_matiere FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_moyennes_matiere ON moyennes_matiere
    USING (eleve_id IN (
        SELECT id FROM eleves WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

ALTER TABLE moyennes_generales ENABLE ROW LEVEL SECURITY;
ALTER TABLE moyennes_generales FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_moyennes_generales ON moyennes_generales
    USING (eleve_id IN (
        SELECT id FROM eleves WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

ALTER TABLE bulletins ENABLE ROW LEVEL SECURITY;
ALTER TABLE bulletins FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_bulletins ON bulletins
    USING (eleve_id IN (
        SELECT id FROM eleves WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));
