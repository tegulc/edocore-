-- ============================================================================
-- Row-Level Security pour le Module 14 (Paiements) et le Module 15 (Comptabilite).
-- moyens_paiement reste hors RLS : referentiel global (comme types_evaluation).
-- ============================================================================

ALTER TABLE structures_frais ENABLE ROW LEVEL SECURITY;
ALTER TABLE structures_frais FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_structures_frais ON structures_frais
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE paiements ENABLE ROW LEVEL SECURITY;
ALTER TABLE paiements FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_paiements ON paiements
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE comptes_comptables ENABLE ROW LEVEL SECURITY;
ALTER TABLE comptes_comptables FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_comptes_comptables ON comptes_comptables
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE journal_comptable ENABLE ROW LEVEL SECURITY;
ALTER TABLE journal_comptable FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_journal_comptable ON journal_comptable
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

-- Tables rattachees indirectement (pas de colonne etablissement_id propre).
ALTER TABLE echeanciers_paiement ENABLE ROW LEVEL SECURITY;
ALTER TABLE echeanciers_paiement FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_echeanciers_paiement ON echeanciers_paiement
    USING (eleve_id IN (
        SELECT id FROM eleves WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

ALTER TABLE tranches_paiement ENABLE ROW LEVEL SECURITY;
ALTER TABLE tranches_paiement FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_tranches_paiement ON tranches_paiement
    USING (echeancier_id IN (
        SELECT ep.id FROM echeanciers_paiement ep
        JOIN eleves e ON e.id = ep.eleve_id
        WHERE e.etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

ALTER TABLE recus ENABLE ROW LEVEL SECURITY;
ALTER TABLE recus FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_recus ON recus
    USING (paiement_id IN (
        SELECT id FROM paiements WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));
