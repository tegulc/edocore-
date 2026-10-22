-- ============================================================================
-- SCRIPT DE DÉMONSTRATION - AFFECTATIONS, ÉVALUATIONS ET NOTES
-- ============================================================================
-- Ce script complète la création des données de démonstration :
-- - Affectation des professeurs aux classes-matières
-- - Création d'évaluations pour chaque classe-matière
-- - Saisie de notes pour chaque élève
-- - Validation des évaluations pour générer les bulletins
-- ============================================================================

SET app.etablissement_id = '2';

-- ============================================================================
-- 1. AFFECTATION DES PROFESSEURS AUX CLASSES-MATIÈRES
-- ============================================================================

-- Récupération des IDs nécessaires
DO $$
DECLARE
    classe_6e_id BIGINT;
    classe_5e_id BIGINT;
    classe_4e_id BIGINT;
    prof_math_id BIGINT;
    prof_fra_id BIGINT;
    prof_ang_id BIGINT;
    prof_phy_id BIGINT;
    prof_svt_id BIGINT;
    prof_hg_id BIGINT;
    prof_eps_id BIGINT;
    prof_info_id BIGINT;
    prof_philo_id BIGINT;
    prof_esp_id BIGINT;
    
    matiere_math_id BIGINT;
    matiere_fra_id BIGINT;
    matiere_ang_id BIGINT;
    matiere_phy_id BIGINT;
    matiere_svt_id BIGINT;
    matiere_hg_id BIGINT;
    matiere_eps_id BIGINT;
    matiere_info_id BIGINT;
    matiere_philo_id BIGINT;
    matiere_esp_id BIGINT;
    
    periode_id BIGINT;
BEGIN
    -- Récupération des classes
    SELECT id INTO classe_6e_id FROM classes WHERE libelle LIKE '6%' AND etablissement_id = 1 LIMIT 1;
    SELECT id INTO classe_5e_id FROM classes WHERE libelle LIKE '5%' AND etablissement_id = 1 LIMIT 1;
    SELECT id INTO classe_4e_id FROM classes WHERE libelle LIKE '4%' AND etablissement_id = 1 LIMIT 1;
    
    -- Récupération des professeurs
    SELECT id INTO prof_math_id FROM enseignants e JOIN utilisateurs u ON e.utilisateur_id = u.id WHERE u.email = 'martin.dupont@educore.cm';
    SELECT id INTO prof_fra_id FROM enseignants e JOIN utilisateurs u ON e.utilisateur_id = u.id WHERE u.email = 'sophie.bernard@educore.cm';
    SELECT id INTO prof_ang_id FROM enseignants e JOIN utilisateurs u ON e.utilisateur_id = u.id WHERE u.email = 'pierre.leroy@educore.cm';
    SELECT id INTO prof_phy_id FROM enseignants e JOIN utilisateurs u ON e.utilisateur_id = u.id WHERE u.email = 'marie.petit@educore.cm';
    SELECT id INTO prof_svt_id FROM enseignants e JOIN utilisateurs u ON e.utilisateur_id = u.id WHERE u.email = 'jean.moreau@educore.cm';
    SELECT id INTO prof_hg_id FROM enseignants e JOIN utilisateurs u ON e.utilisateur_id = u.id WHERE u.email = 'claire.dubois@educore.cm';
    SELECT id INTO prof_eps_id FROM enseignants e JOIN utilisateurs u ON e.utilisateur_id = u.id WHERE u.email = 'luc.garnier@educore.cm';
    SELECT id INTO prof_info_id FROM enseignants e JOIN utilisateurs u ON e.utilisateur_id = u.id WHERE u.email = 'nathalie.fournier@educore.cm';
    SELECT id INTO prof_philo_id FROM enseignants e JOIN utilisateurs u ON e.utilisateur_id = u.id WHERE u.email = 'philippe.martinez@educore.cm';
    SELECT id INTO prof_esp_id FROM enseignants e JOIN utilisateurs u ON e.utilisateur_id = u.id WHERE u.email = 'elena.garcia@educore.cm';
    
    -- Récupération des matières
    SELECT id INTO matiere_math_id FROM matieres WHERE code = 'MATH' LIMIT 1;
    SELECT id INTO matiere_fra_id FROM matieres WHERE code = 'FRA' LIMIT 1;
    SELECT id INTO matiere_ang_id FROM matieres WHERE code = 'ANG' LIMIT 1;
    SELECT id INTO matiere_phy_id FROM matieres WHERE code = 'PHYCHIM' LIMIT 1;
    SELECT id INTO matiere_svt_id FROM matieres WHERE code = 'SVT' LIMIT 1;
    SELECT id INTO matiere_hg_id FROM matieres WHERE code = 'HISTGEO' LIMIT 1;
    SELECT id INTO matiere_eps_id FROM matieres WHERE code = 'EPS' LIMIT 1;
    SELECT id INTO matiere_info_id FROM matieres WHERE code = 'INFO' LIMIT 1;
    SELECT id INTO matiere_philo_id FROM matieres WHERE code = 'PHILO' LIMIT 1;
    SELECT id INTO matiere_esp_id FROM matieres WHERE code = 'ESP' LIMIT 1;
    
    -- Récupération de la période (1er trimestre)
    SELECT id INTO periode_id FROM periodes p 
    JOIN annees_scolaires a ON p.annee_scolaire_id = a.id 
    WHERE a.libelle = '2024-2025' AND p.numero = 1 LIMIT 1;
    
    -- Affectation pour 6ème A
    IF classe_6e_id IS NOT NULL THEN
        -- Créer les classe_matiere si n'existent pas
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_6e_id, matiere_math_id, 4, 4, prof_math_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_math_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_6e_id, matiere_fra_id, 4, 5, prof_fra_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_fra_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_6e_id, matiere_ang_id, 3, 3, prof_ang_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_ang_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_6e_id, matiere_phy_id, 3, 3, prof_phy_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_phy_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_6e_id, matiere_svt_id, 3, 3, prof_svt_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_svt_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_6e_id, matiere_hg_id, 3, 3, prof_hg_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_hg_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_6e_id, matiere_eps_id, 2, 2, prof_eps_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_eps_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_6e_id, matiere_info_id, 2, 2, prof_info_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_info_id;
    END IF;
    
    -- Affectation pour 5ème B
    IF classe_5e_id IS NOT NULL THEN
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_5e_id, matiere_math_id, 4, 4, prof_math_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_math_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_5e_id, matiere_fra_id, 4, 5, prof_fra_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_fra_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_5e_id, matiere_ang_id, 3, 3, prof_ang_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_ang_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_5e_id, matiere_phy_id, 3, 3, prof_phy_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_phy_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_5e_id, matiere_svt_id, 3, 3, prof_svt_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_svt_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_5e_id, matiere_hg_id, 3, 3, prof_hg_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_hg_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_5e_id, matiere_eps_id, 2, 2, prof_eps_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_eps_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_5e_id, matiere_info_id, 2, 2, prof_info_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_info_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_5e_id, matiere_esp_id, 2, 2, prof_esp_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_esp_id;
    END IF;
    
    -- Affectation pour 4ème C
    IF classe_4e_id IS NOT NULL THEN
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_4e_id, matiere_math_id, 4, 4, prof_math_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_math_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_4e_id, matiere_fra_id, 4, 5, prof_fra_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_fra_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_4e_id, matiere_ang_id, 3, 3, prof_ang_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_ang_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_4e_id, matiere_phy_id, 3, 3, prof_phy_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_phy_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_4e_id, matiere_svt_id, 3, 3, prof_svt_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_svt_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_4e_id, matiere_hg_id, 3, 3, prof_hg_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_hg_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_4e_id, matiere_eps_id, 2, 2, prof_eps_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_eps_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_4e_id, matiere_info_id, 2, 2, prof_info_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_info_id;
        
        INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo, enseignant_id)
        VALUES (classe_4e_id, matiere_philo_id, 2, 2, prof_philo_id)
        ON CONFLICT (classe_id, matiere_id) DO UPDATE SET enseignant_id = prof_philo_id;
    END IF;
    
    RAISE NOTICE 'Affectations des professeurs aux classes-matières terminées';
END $$;

-- ============================================================================
-- 2. CRÉATION DES ÉVALUATIONS POUR CHAQUE CLASSE-MATIÈRE
-- ============================================================================

DO $$
DECLARE
    classe_matiere_record RECORD;
    periode_id BIGINT;
    evaluation_id BIGINT;
BEGIN
    SELECT id INTO periode_id FROM periodes p 
    JOIN annees_scolaires a ON p.annee_scolaire_id = a.id 
    WHERE a.libelle = '2024-2025' AND p.numero = 1 LIMIT 1;
    
    -- Créer une évaluation pour chaque classe_matière
    FOR classe_matiere_record IN 
        SELECT cm.id, m.code as matiere_code, c.libelle as classe_libelle 
        FROM classe_matiere cm
        JOIN classes c ON cm.classe_id = c.id
        JOIN matieres m ON cm.matiere_id = m.id
        WHERE c.etablissement_id = 1
    LOOP
        -- Créer évaluation devoir
        INSERT INTO evaluations (classe_matiere_id, type_evaluation_code, periode_id, titre, date_evaluation, bareme, statut)
        VALUES (
            classe_matiere_record.id,
            'DEVOIR',
            periode_id,
            'Devoir ' || classe_matiere_record.matiere_code || ' - ' || classe_matiere_record.classe_libelle,
            '2024-10-15',
            20,
            'brouillon'
        )
        RETURNING id INTO evaluation_id;
        
        RAISE NOTICE 'Évaluation créée: % pour classe %', classe_matiere_record.matiere_code, classe_matiere_record.classe_libelle;
    END LOOP;
    
    RAISE NOTICE 'Création des évaluations terminée';
END $$;

-- ============================================================================
-- 3. SAISIE DES NOTES POUR CHAQUE ÉLÈVE DANS CHAQUE MATIÈRE
-- ============================================================================

DO $$
DECLARE
    evaluation_record RECORD;
    eleve_record RECORD;
    note_value NUMERIC;
BEGIN
    -- Pour chaque évaluation
    FOR evaluation_record IN 
        SELECT e.id, e.bareme, cm.classe_id 
        FROM evaluations e
        JOIN classe_matiere cm ON e.classe_matiere_id = cm.id
        JOIN classes c ON cm.classe_id = c.id
        WHERE c.etablissement_id = 1 AND e.statut = 'brouillon'
    LOOP
        -- Pour chaque élève de la classe
        FOR eleve_record IN 
            SELECT ele.id, ele.matricule 
            FROM eleves ele
            JOIN inscriptions i ON ele.id = i.eleve_id
            WHERE i.classe_id = evaluation_record.classe_id AND i.statut = 'validee'
        LOOP
            -- Générer une note aléatoire entre 8 et 18 (pour avoir des élèves > 12)
            note_value := 8 + (random() * 10);
            
            -- Insérer la note
            INSERT INTO notes (evaluation_id, eleve_id, valeur, absent, commentaire)
            VALUES (
                evaluation_record.id,
                eleve_record.id,
                note_value,
                false,
                'Note de démonstration'
            );
        END LOOP;
        
        RAISE NOTICE 'Notes saisies pour évaluation ID: %', evaluation_record.id;
    END LOOP;
    
    RAISE NOTICE 'Saisie des notes terminée';
END $$;

-- ============================================================================
-- 4. VALIDATION DES ÉVALUATIONS
-- ============================================================================

DO $$
BEGIN
    -- Valider toutes les évaluations en brouillon
    UPDATE evaluations 
    SET statut = 'validee', 
        date_validation = CURRENT_TIMESTAMP
    WHERE statut = 'brouillon';
    
    RAISE NOTICE 'Validation des évaluations terminée';
END $$;

-- ============================================================================
-- 5. CALCUL DES MOYENNES
-- ============================================================================

-- Le calcul des moyennes se fait automatiquement via les services applicatifs
-- Cette partie nécessite l'exécution via l'API Java

-- ============================================================================
-- RÉSUMÉ
-- ============================================================================
-- - Affectation des professeurs aux classes-matières : terminée
-- - Création des évaluations : terminée
-- - Saisie des notes : terminée
-- - Validation des évaluations : terminée
-- - Calcul des moyennes : à exécuter via l'API
-- ============================================================================
