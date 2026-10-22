-- ============================================================================
-- INSCRIPTION DES ÉLÈVES DANS LES CLASSES
-- ============================================================================

SET app.etablissement_id = '2';

-- Inscription des élèves 1-12 en 6ème A
DO $$
DECLARE
    i INTEGER;
    classe_6e_id BIGINT;
    annee_scolaire_id BIGINT;
BEGIN
    SELECT id INTO classe_6e_id FROM classes WHERE libelle = '6ème A' AND etablissement_id = 2 LIMIT 1;
    SELECT id INTO annee_scolaire_id FROM annees_scolaires WHERE est_active = true AND etablissement_id = 2 LIMIT 1;
    
    FOR i IN 1..12 LOOP
        INSERT INTO inscriptions (eleve_id, annee_scolaire_id, classe_id, type_inscription, date_inscription, statut)
        VALUES ((SELECT id FROM eleves WHERE matricule = 'ELE' || LPAD(i::TEXT, 3, '0')), 
                annee_scolaire_id, classe_6e_id, 'inscription', '2024-09-01', 'validee');
    END LOOP;
    
    RAISE NOTICE 'Inscription des élèves en 6ème A terminée';
END $$;

-- Inscription des élèves 13-22 en 5ème B
DO $$
DECLARE
    i INTEGER;
    classe_5e_id BIGINT;
    annee_scolaire_id BIGINT;
BEGIN
    SELECT id INTO classe_5e_id FROM classes WHERE libelle = '5ème B' AND etablissement_id = 2 LIMIT 1;
    SELECT id INTO annee_scolaire_id FROM annees_scolaires WHERE est_active = true AND etablissement_id = 2 LIMIT 1;
    
    FOR i IN 13..22 LOOP
        INSERT INTO inscriptions (eleve_id, annee_scolaire_id, classe_id, type_inscription, date_inscription, statut)
        VALUES ((SELECT id FROM eleves WHERE matricule = 'ELE' || LPAD(i::TEXT, 3, '0')), 
                annee_scolaire_id, classe_5e_id, 'inscription', '2024-09-01', 'validee');
    END LOOP;
    
    RAISE NOTICE 'Inscription des élèves en 5ème B terminée';
END $$;

-- Inscription des élèves 23-30 en 4ème C
DO $$
DECLARE
    i INTEGER;
    classe_4e_id BIGINT;
    annee_scolaire_id BIGINT;
BEGIN
    SELECT id INTO classe_4e_id FROM classes WHERE libelle = '4ème C' AND etablissement_id = 2 LIMIT 1;
    SELECT id INTO annee_scolaire_id FROM annees_scolaires WHERE est_active = true AND etablissement_id = 2 LIMIT 1;
    
    FOR i IN 23..30 LOOP
        INSERT INTO inscriptions (eleve_id, annee_scolaire_id, classe_id, type_inscription, date_inscription, statut)
        VALUES ((SELECT id FROM eleves WHERE matricule = 'ELE' || LPAD(i::TEXT, 3, '0')), 
                annee_scolaire_id, classe_4e_id, 'inscription', '2024-09-01', 'validee');
    END LOOP;
    
    RAISE NOTICE 'Inscription des élèves en 4ème C terminée';
END $$;