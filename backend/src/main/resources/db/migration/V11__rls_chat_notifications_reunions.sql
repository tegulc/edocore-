-- ============================================================================
-- Row-Level Security pour les tables consommees par le notifications-service
-- (conversations, messages, notifications) ainsi que le module Reunions,
-- reste dans le monolithe. educore-backend demeure la source de verite
-- unique du schema (Flyway), meme pour les tables que d'autres services
-- consomment ensuite (voir README, section separation Chat/Notifications).
-- ============================================================================

ALTER TABLE conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE conversations FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_conversations ON conversations
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_notifications ON notifications
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

-- Tables rattachees indirectement.
ALTER TABLE conversation_participants ENABLE ROW LEVEL SECURITY;
ALTER TABLE conversation_participants FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_conversation_participants ON conversation_participants
    USING (conversation_id IN (
        SELECT id FROM conversations WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

ALTER TABLE messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_messages ON messages
    USING (conversation_id IN (
        SELECT id FROM conversations WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

ALTER TABLE appareils_mobiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE appareils_mobiles FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_appareils_mobiles ON appareils_mobiles
    USING (utilisateur_id IN (
        SELECT id FROM utilisateurs WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));

-- Module 23 (Reunions), reste dans le monolithe.
ALTER TABLE reunions ENABLE ROW LEVEL SECURITY;
ALTER TABLE reunions FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_reunions ON reunions
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

ALTER TABLE reunion_invites ENABLE ROW LEVEL SECURITY;
ALTER TABLE reunion_invites FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_reunion_invites ON reunion_invites
    USING (reunion_id IN (
        SELECT id FROM reunions WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));
