-- ============================================================================
-- Row-Level Security pour le Module 5 (structure pedagogique) et les
-- enseignants, suivant exactement le meme schema que V2 (voir ce fichier
-- pour la justification complete du mecanisme).
-- ============================================================================

ALTER TABLE niveaux ENABLE ROW LEVEL SECURITY;
ALTER TABLE niveaux FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_niveaux ON niveaux
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE series ENABLE ROW LEVEL SECURITY;
ALTER TABLE series FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_series ON series
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE salles ENABLE ROW LEVEL SECURITY;
ALTER TABLE salles FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_salles ON salles
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE matieres ENABLE ROW LEVEL SECURITY;
ALTER TABLE matieres FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_matieres ON matieres
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE classes ENABLE ROW LEVEL SECURITY;
ALTER TABLE classes FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_classes ON classes
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE enseignants ENABLE ROW LEVEL SECURITY;
ALTER TABLE enseignants FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_enseignants ON enseignants
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

-- classe_matiere n'a pas de colonne etablissement_id propre (rattachee via classe_id).
ALTER TABLE classe_matiere ENABLE ROW LEVEL SECURITY;
ALTER TABLE classe_matiere FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_classe_matiere ON classe_matiere
    USING (classe_id IN (
        SELECT id FROM classes WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));
