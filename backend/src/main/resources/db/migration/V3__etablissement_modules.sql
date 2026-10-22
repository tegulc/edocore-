-- ============================================================================
-- Activation des modules par etablissement (cahier des charges, 3.1
-- Administrateur : "Parametrer les modules actifs pour l'etablissement").
-- Table absente du script SQL initial, ajoutee ici pour ce besoin explicite.
-- ============================================================================

CREATE TABLE etablissement_modules (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    module_code         VARCHAR(50) NOT NULL,
    actif               BOOLEAN NOT NULL DEFAULT false,
    date_maj            TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (etablissement_id, module_code)
);

CREATE INDEX idx_etablissement_modules ON etablissement_modules(etablissement_id);

ALTER TABLE etablissement_modules ENABLE ROW LEVEL SECURITY;
ALTER TABLE etablissement_modules FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_etablissement_modules ON etablissement_modules
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);
