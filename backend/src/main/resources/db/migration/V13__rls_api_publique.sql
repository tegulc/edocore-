-- ============================================================================
-- Row-Level Security pour le Module 30 (API publique, Phase 9).
-- ============================================================================

ALTER TABLE api_clients ENABLE ROW LEVEL SECURITY;
ALTER TABLE api_clients FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_api_clients ON api_clients
    USING (etablissement_id = current_setting('app.etablissement_id', true)::bigint);

-- api_logs : rattachee via client_id -> api_clients.etablissement_id.
ALTER TABLE api_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE api_logs FORCE ROW LEVEL SECURITY;
CREATE POLICY isolation_api_logs ON api_logs
    USING (api_client_id IN (
        SELECT id FROM api_clients WHERE etablissement_id = current_setting('app.etablissement_id', true)::bigint
    ));
