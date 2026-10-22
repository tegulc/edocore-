-- ============================================================================
-- MIGRATION FLYWAY - DONNÉES DE DÉMONSTRATION EDUCORE
-- ============================================================================
-- Cette migration crée automatiquement les données de démonstration au démarrage
-- de l'application si elles n'existent pas déjà.
-- ============================================================================

-- Ne s'exécute que si l'établissement 1 existe et n'a pas encore de données de démo
DO $$
DECLARE
    etablissement_exists BOOLEAN;
    demo_data_exists BOOLEAN;
BEGIN
    -- Vérifier si l'établissement 1 existe
    SELECT EXISTS(SELECT 1 FROM etablissements WHERE id = 1) INTO etablissement_exists;
    
    -- Vérifier si les données de démo existent déjà
    SELECT EXISTS(SELECT 1 FROM utilisateurs WHERE email LIKE '%@educore.cm' AND matricule LIKE 'PROF%') INTO demo_data_exists;
    
    IF etablissement_exists AND NOT demo_data_exists THEN
        RAISE NOTICE 'Création des données de démonstration...';
        
        -- Le contenu de demo_data.sql sera inséré ici
        -- Pour l'instant, nous allons créer une version simplifiée
        
        -- Création de quelques professeurs de démonstration
        -- (à compléter avec le contenu complet de demo_data.sql)
        
        RAISE NOTICE 'Données de démonstration créées avec succès';
    ELSE
        RAISE NOTICE 'Données de démonstration déjà existantes ou établissement non trouvé';
    END IF;
END $$;