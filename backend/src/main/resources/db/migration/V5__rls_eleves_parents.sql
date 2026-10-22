-- ============================================================================
-- Row-Level Security pour le Module 2 (Eleves) et le Module 3 (Parents).
-- Meme mecanisme que V2/V4 (voir V2 pour la justification complete).
-- ============================================================================

ALTER TABLE eleves ENABLE ROW LEVEL SECURITY;
ALTER TABLE eleves FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_eleves ON eleves
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE parents ENABLE ROW LEVEL SECURITY;
ALTER TABLE parents FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_parents ON parents
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

-- inscriptions n'a pas de colonne etablissement_id propre (rattachee via eleve_id).
ALTER TABLE inscriptions ENABLE ROW LEVEL SECURITY;
ALTER TABLE inscriptions FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_inscriptions ON inscriptions
    USING (eleve_id IN (
        SELECT id FROM eleves WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

-- eleve_parent : idem, rattachee via eleve_id.
ALTER TABLE eleve_parent ENABLE ROW LEVEL SECURITY;
ALTER TABLE eleve_parent FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_eleve_parent ON eleve_parent
    USING (eleve_id IN (
        SELECT id FROM eleves WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));
