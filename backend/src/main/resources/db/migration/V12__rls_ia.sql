-- ============================================================================
-- Row-Level Security pour le Module 25 (Intelligence Artificielle), consomme
-- par ia-service (Phase 8) - meme mecanisme que V2. educore-backend reste
-- l'unique proprietaire du schema, y compris pour les tables que ia-service
-- consomme ensuite (voir README, section separation IA).
-- ============================================================================

CREATE OR REPLACE FUNCTION _educore_appartient_etablissement_eleve(p_eleve_id BIGINT) RETURNS BOOLEAN AS $$
    SELECT EXISTS (
        SELECT 1 FROM eleves WHERE id = p_eleve_id AND etablissement_id = current_setting('app.etablissement_id', true)::bigint
    );
$$ LANGUAGE sql STABLE;
-- (fonction deja creee en V10 si cette migration s'execute apres - CREATE OR REPLACE evite toute erreur de rejeu)

ALTER TABLE ia_alertes_risque_echec ENABLE ROW LEVEL SECURITY;
ALTER TABLE ia_alertes_risque_echec FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_ia_alertes_risque_echec ON ia_alertes_risque_echec USING (_educore_appartient_etablissement_eleve(eleve_id));

ALTER TABLE ia_recommandations_orientation ENABLE ROW LEVEL SECURITY;
ALTER TABLE ia_recommandations_orientation FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_ia_recommandations_orientation ON ia_recommandations_orientation USING (_educore_appartient_etablissement_eleve(eleve_id));

ALTER TABLE ia_exercices_personnalises ENABLE ROW LEVEL SECURITY;
ALTER TABLE ia_exercices_personnalises FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_ia_exercices_personnalises ON ia_exercices_personnalises USING (_educore_appartient_etablissement_eleve(eleve_id));

ALTER TABLE ia_predictions_examens ENABLE ROW LEVEL SECURITY;
ALTER TABLE ia_predictions_examens FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_ia_predictions_examens ON ia_predictions_examens USING (_educore_appartient_etablissement_eleve(eleve_id));

-- ia_commentaires_bulletin : rattachee via bulletin_id -> eleve_id.
ALTER TABLE ia_commentaires_bulletin ENABLE ROW LEVEL SECURITY;
ALTER TABLE ia_commentaires_bulletin FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_ia_commentaires_bulletin ON ia_commentaires_bulletin
    USING (bulletin_id IN (
        SELECT b.id FROM bulletins b WHERE _educore_appartient_etablissement_eleve(b.eleve_id)
    ));

-- ia_conversations_assistant : rattachee via utilisateur_id.
ALTER TABLE ia_conversations_assistant ENABLE ROW LEVEL SECURITY;
ALTER TABLE ia_conversations_assistant FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_ia_conversations_assistant ON ia_conversations_assistant
    USING (utilisateur_id IN (
        SELECT id FROM utilisateurs WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));
