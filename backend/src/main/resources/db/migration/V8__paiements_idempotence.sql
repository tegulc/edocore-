-- ============================================================================
-- Idempotence des paiements (Volume 6, Documentation API, Partie IV.3 :
-- en-tete Idempotency-Key obligatoire sur POST /paiements/initier).
-- Table absente du script SQL initial, ajoutee ici pour ce besoin explicite.
-- ============================================================================

CREATE TABLE cles_idempotence (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    cle                 VARCHAR(100) NOT NULL,
    paiement_id         BIGINT NOT NULL REFERENCES paiements(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (etablissement_id, cle)
);

INSERT INTO moyens_paiement (code) VALUES
('orange_money'), ('mtn_momo'), ('carte_bancaire'), ('virement'), ('especes')
ON CONFLICT (code) DO NOTHING;

ALTER TABLE cles_idempotence ENABLE ROW LEVEL SECURITY;
ALTER TABLE cles_idempotence FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_cles_idempotence ON cles_idempotence
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);
