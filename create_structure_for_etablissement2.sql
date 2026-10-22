-- ============================================================================
-- CRÉATION DE LA STRUCTURE POUR L'ÉTABLISSEMENT 2
-- ============================================================================

SET app.etablissement_id = '2';

-- Créer les niveaux pour l'établissement 2
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
    
    -- Créer les niveaux s'ils n'existent pas
    INSERT INTO niveaux (etablissement_id, code, libelle, ordre, cycle)
    VALUES (2, '6e', '6ème', 1, 'premier_cycle')
    ON CONFLICT (etablissement_id, code) DO NOTHING;
    
    INSERT INTO niveaux (etablissement_id, code, libelle, ordre, cycle)
    VALUES (2, '5e', '5ème', 2, 'premier_cycle')
    ON CONFLICT (etablissement_id, code) DO NOTHING;
    
    INSERT INTO niveaux (etablissement_id, code, libelle, ordre, cycle)
    VALUES (2, '4e', '4ème', 3, 'premier_cycle')
    ON CONFLICT (etablissement_id, code) DO NOTHING;
    
    RAISE NOTICE 'Niveaux créés pour l''établissement 2';
END $$;

-- Créer les classes
DO $$
DECLARE
    v_annee_scolaire_id BIGINT;
    niveau_6e_id BIGINT;
    niveau_5e_id BIGINT;
    niveau_4e_id BIGINT;
BEGIN
    -- Récupérer l'année scolaire active
    SELECT id INTO v_annee_scolaire_id FROM annees_scolaires WHERE est_active = true AND etablissement_id = 2 LIMIT 1;
    
    -- Récupérer les niveaux de l'établissement 2
    SELECT id INTO niveau_6e_id FROM niveaux WHERE code = '6e' AND etablissement_id = 2 LIMIT 1;
    SELECT id INTO niveau_5e_id FROM niveaux WHERE code = '5e' AND etablissement_id = 2 LIMIT 1;
    SELECT id INTO niveau_4e_id FROM niveaux WHERE code = '4e' AND etablissement_id = 2 LIMIT 1;
    
    -- Créer les classes
    IF niveau_6e_id IS NOT NULL AND v_annee_scolaire_id IS NOT NULL THEN
        INSERT INTO classes (etablissement_id, annee_scolaire_id, niveau_id, libelle, capacite)
        VALUES (2, v_annee_scolaire_id, niveau_6e_id, '6ème A', 30)
        ON CONFLICT (etablissement_id, annee_scolaire_id, libelle) DO NOTHING;
        RAISE NOTICE 'Classe 6ème A créée';
    END IF;
    
    IF niveau_5e_id IS NOT NULL AND v_annee_scolaire_id IS NOT NULL THEN
        INSERT INTO classes (etablissement_id, annee_scolaire_id, niveau_id, libelle, capacite)
        VALUES (2, v_annee_scolaire_id, niveau_5e_id, '5ème B', 30)
        ON CONFLICT (etablissement_id, annee_scolaire_id, libelle) DO NOTHING;
        RAISE NOTICE 'Classe 5ème B créée';
    END IF;
    
    IF niveau_4e_id IS NOT NULL AND v_annee_scolaire_id IS NOT NULL THEN
        INSERT INTO classes (etablissement_id, annee_scolaire_id, niveau_id, libelle, capacite)
        VALUES (2, v_annee_scolaire_id, niveau_4e_id, '4ème C', 30)
        ON CONFLICT (etablissement_id, annee_scolaire_id, libelle) DO NOTHING;
        RAISE NOTICE 'Classe 4ème C créée';
    END IF;
    
    RAISE NOTICE 'Création des classes terminée';
END $$;