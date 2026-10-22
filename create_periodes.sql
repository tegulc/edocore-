-- ============================================================================
-- CRÉATION DES PÉRIODES POUR L'ÉTABLISSEMENT 2
-- ============================================================================

SET app.etablissement_id = '2';

-- Créer les périodes (trimestres) pour l'année scolaire active
DO $$
DECLARE
    annee_scolaire_id BIGINT;
BEGIN
    -- Récupérer l'année scolaire active
    SELECT id INTO annee_scolaire_id FROM annees_scolaires WHERE est_active = true AND etablissement_id = 2 LIMIT 1;
    
    IF annee_scolaire_id IS NULL THEN
        RAISE NOTICE 'Aucune année scolaire active trouvée pour l''établissement 2';
        RETURN;
    END IF;
    
    -- Créer les 3 trimestres
    INSERT INTO periodes (annee_scolaire_id, type_periode, numero, libelle, date_debut, date_fin, est_cloturee)
    VALUES (annee_scolaire_id, 'trimestre', 1, '1er Trimestre', '2026-09-01', '2026-12-15', false)
    ON CONFLICT (annee_scolaire_id, type_periode, numero) DO NOTHING;
    
    INSERT INTO periodes (annee_scolaire_id, type_periode, numero, libelle, date_debut, date_fin, est_cloturee)
    VALUES (annee_scolaire_id, 'trimestre', 2, '2ème Trimestre', '2027-01-02', '2027-03-31', false)
    ON CONFLICT (annee_scolaire_id, type_periode, numero) DO NOTHING;
    
    INSERT INTO periodes (annee_scolaire_id, type_periode, numero, libelle, date_debut, date_fin, est_cloturee)
    VALUES (annee_scolaire_id, 'trimestre', 3, '3ème Trimestre', '2027-04-01', '2027-07-31', false)
    ON CONFLICT (annee_scolaire_id, type_periode, numero) DO NOTHING;
    
    RAISE NOTICE 'Périodes créées pour l''établissement 2';
END $$;