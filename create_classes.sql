-- ============================================================================
-- CRÉATION DES CLASSES POUR L'ÉTABLISSEMENT 2
-- ============================================================================

SET app.etablissement_id = '2';

-- Récupérer l'année scolaire active et les niveaux
DO $$
DECLARE
    annee_scolaire_id BIGINT;
    niveau_6e_id BIGINT;
    niveau_5e_id BIGINT;
    niveau_4e_id BIGINT;
    classe_6e_id BIGINT;
    classe_5e_id BIGINT;
    classe_4e_id BIGINT;
BEGIN
    -- Récupérer l'année scolaire active
    SELECT id INTO annee_scolaire_id FROM annees_scolaires WHERE est_active = true AND etablissement_id = 2 LIMIT 1;
    
    -- Récupérer les niveaux
    SELECT id INTO niveau_6e_id FROM niveaux WHERE code = '6e' AND (etablissement_id = 2 OR etablissement_id IS NULL) LIMIT 1;
    SELECT id INTO niveau_5e_id FROM niveaux WHERE code = '5e' AND (etablissement_id = 2 OR etablissement_id IS NULL) LIMIT 1;
    SELECT id INTO niveau_4e_id FROM niveaux WHERE code = '4e' AND (etablissement_id = 2 OR etablissement_id IS NULL) LIMIT 1;
    
    -- Créer les classes si elles n'existent pas
    IF niveau_6e_id IS NOT NULL AND annee_scolaire_id IS NOT NULL THEN
        INSERT INTO classes (etablissement_id, annee_scolaire_id, niveau_id, libelle, capacite)
        VALUES (2, annee_scolaire_id, niveau_6e_id, '6ème A', 30)
        ON CONFLICT (etablissement_id, annee_scolaire_id, libelle) DO NOTHING;
        
        SELECT id INTO classe_6e_id FROM classes WHERE libelle = '6ème A' AND etablissement_id = 2 AND annee_scolaire_id = annee_scolaire_id;
        RAISE NOTICE 'Classe 6ème A créée avec ID: %', classe_6e_id;
    END IF;
    
    IF niveau_5e_id IS NOT NULL AND annee_scolaire_id IS NOT NULL THEN
        INSERT INTO classes (etablissement_id, annee_scolaire_id, niveau_id, libelle, capacite)
        VALUES (2, annee_scolaire_id, niveau_5e_id, '5ème B', 30)
        ON CONFLICT (etablissement_id, annee_scolaire_id, libelle) DO NOTHING;
        
        SELECT id INTO classe_5e_id FROM classes WHERE libelle = '5ème B' AND etablissement_id = 2 AND annee_scolaire_id = annee_scolaire_id;
        RAISE NOTICE 'Classe 5ème B créée avec ID: %', classe_5e_id;
    END IF;
    
    IF niveau_4e_id IS NOT NULL AND annee_scolaire_id IS NOT NULL THEN
        INSERT INTO classes (etablissement_id, annee_scolaire_id, niveau_id, libelle, capacite)
        VALUES (2, annee_scolaire_id, niveau_4e_id, '4ème C', 30)
        ON CONFLICT (etablissement_id, annee_scolaire_id, libelle) DO NOTHING;
        
        SELECT id INTO classe_4e_id FROM classes WHERE libelle = '4ème C' AND etablissement_id = 2 AND annee_scolaire_id = annee_scolaire_id;
        RAISE NOTICE 'Classe 4ème C créée avec ID: %', classe_4e_id;
    END IF;
    
    RAISE NOTICE 'Création des classes terminée';
END $$;